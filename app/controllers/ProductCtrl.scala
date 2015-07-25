package controllers

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.{JsonPagelet, BigPipe, HtmlPagelet}
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import net.sf.ehcache.Ehcache
import play.api.Configuration


import play.api.libs.ws._

import play.api.cache._
import play.api.Play.current
import java.util.UUID
import javax.inject.Inject
import core.dao.DocumentDAO
import core.dao.DocumentDAO._
import models.Product
import org.joda.time.DateTime
import pjax.Pjax
import play.api.http.HeaderNames
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Promise
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.api.QueryOpts
import reactivemongo.api.indexes.{IndexType, Index}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands._
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

import scala.util.Random
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

class ProductCtrl @Inject() (
     val messagesApi: MessagesApi,
     val reactiveMongoApi: ReactiveMongoApi,
     ws: WSClient,
     cache: CacheApi,
     cached: Cached)
    extends Controller with MongoController with ReactiveMongoComponents with Pjax{

  //--------------------    SETUP   ------------------------------------------------------------
  val cProduct = {
    val cProduct = db[JSONCollection]("product")
    cProduct.indexesManager.ensure(
      Index(List("code" -> IndexType.Ascending), unique = true)
    )
    cProduct
  }

  var cacheList = scala.collection.mutable.ListBuffer.empty[String]

  //-------------------    CREATE PRODUCT    ---------------------------------------------------------
  def viewCreate = Action { implicit request =>
    Ok(views.html.product.create(Product.form, ""))
  }

  def create = Action.async { implicit request =>

    Product.form.bindFromRequest.fold(
      errors => Future.successful(
        BadGateway(views.html.product.create(errors, ""))),

      // if no error, then insert the article into the 'articles' collection
      product => {
        val tmp = product.copy(
          id = product.id.orElse(Some(UUID.randomUUID().toString)),
          creationDate = Some(new DateTime()),
          updateDate = Some(new DateTime())
        )
        val f = insert[Product](cProduct, tmp)
        f.map{
          lastError => {
            clearCache()
            Ok("ok")
          }
        }.recover{
          case e => {
            Ok(views.html.product.create(Product.form.fill(product), e.getMessage()))
          }
        }
      }
    )
  }

  //-------------------------  delete product --------------------------------------------------------

  def deleteProduct(id: String) = PjaxAction.async { implicit  request =>
    cProduct.remove(Json.obj("_id" -> id))
    .map(_ => {
      clearCache()
      Redirect(routes.ProductCtrl.listProduct(1, 10, "", "", ""))
    }).recover {
      case _ => InternalServerError
    }
  }

  //-------------------------  Edit product ---------------------------------------------------------
  def viewEdit(pUrl: String) = PjaxAction.async { implicit request =>
    println(pUrl)
    val futureProduct = cProduct.find(Json.obj("pUrl" -> pUrl))
        .cursor[Product]().headOption
    futureProduct.map {
      p => p match {
        case None => Ok("san pham khong ton tai")
        case Some(product) => Ok(views.html.product.edit(Product.form.fill(product), ""))
      }
    }
  }

  def update(pUrl: String) = PjaxAction.async {implicit request =>
    import reactivemongo.bson.BSONDateTime
    Product.form.bindFromRequest.fold(
      errors => Future.successful(
        BadGateway(views.html.product.create(errors, " ??????????????"))),

      // if no error, then insert the article into the 'articles' collection
      product => {
        // create a modifier document, ie a document that contains the update operations to run onto the documents matching the query
        val modifier = Json.obj(
          // this modifier will set the fields
          // 'updateDate', 'title', 'content', and 'publisher'
          "$set" -> Json.obj(
            "supTypeUrl" -> product.supTypeUrl,
            "supType" -> product.supType,
            "subTypeUrl" -> product.subTypeUrl,
            "subType" -> product.subType,
            "pUrl" -> product.pUrl,
            "image" -> Json.toJsFieldJsValueWrapper(product.image),
            "code" -> product.code,
            "name" -> product.name,
            "unit" -> product.unit,
            "stock" -> product.stock,
            "price" -> product.price,
            "groupUrl" -> product.groupUrl,
            "group" -> product.group,
            "brand" -> product.brand,
            "origin" -> product.origin,
            "legType" -> product.legType,
            "legNumber" -> product.legNumber,
            "info" -> product.info,
            "note" -> product.note,
            "updateDate" -> BSONDateTime(new DateTime().getMillis)
          )
        )

        // ok, let's do the update
        cProduct.update(Json.obj("pUrl" -> pUrl), modifier).
            map { _ => {
                clearCache()
                Redirect(routes.ProductCtrl.viewEdit(product.pUrl)).flashing("success" -> "update OK!")
              }
            }
      }
    )
  }

  //-------------------------  Index page   ----------------------------------------------------------

  def index2 = PjaxAction.async { implicit request =>
    val futureColection1 = cProduct.find(Json.obj("supTypeUrl" -> "phan-cung-thiet-bi"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)
    val futureColection2 = cProduct.find(Json.obj("supTypeUrl" -> "ban-dan-cam-bien"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)

    val futureColection3 = cProduct.find(Json.obj("supTypeUrl" -> "lk-khac-phu-kien"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)


    val pageletColelction1 = HtmlPagelet("collection1", futureColection1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", futureColection2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", futureColection3.map(x => views.html.product.collection(x)))

    val futureAside = Future(1)
//    val delay4 = 0.25
//    val futureAside = Promise.timeout(futureAside2, delay4.second).flatMap(x => x)

    val aside = HtmlPagelet("aside", futureAside.map(_ => views.html.partials.aside()))

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3, aside)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3, aside)))
  }

  def index = PjaxAction.async { implicit request =>

    val futureColection1 = cache.get[List[Product]]( "index1" ) match {
      case None => {
        println("Not found index1 result in cache")
        val futureColection1 = cProduct.find(Json.obj("supTypeUrl" -> "phan-cung-thiet-bi"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](8)

        futureColection1.map{
          list => cache.set("index1", list, 30 days)
            cacheList += "index1"
        }
        futureColection1
      }

      case Some(p) => {
        println("found index1 result")
        Future(p)
      }
    }

    val futureColection2 = cache.get[List[Product]]( "index2" ) match {
      case None => {
        println("Not found index2 result in cache")
        val futureColection2 = cProduct.find(Json.obj("supTypeUrl" -> "ban-dan-cam-bien"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](8)

        futureColection2.map{
          list => cache.set("index2", list, 30 days)
          cacheList += "index2"
        }
        futureColection2
      }
      case Some(p) => {
        println("found index2 result")
        Future(p)
      }
    }

    val futureColection3 = cache.get[List[Product]]( "index3" ) match {
      case None => {
        println("Not found index3 result in cache")

        val futureColection3 = cProduct.find(Json.obj("supTypeUrl" -> "lk-khac-phu-kien"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](8)

        futureColection3.map{

          list => cache.set("index3", list, 30 days)

            cacheList += "index3"        }
        futureColection2
      }

      case Some(p) => {
        println("found index3 result")
        Future(p)
      }
    }

    val pageletColelction1 = HtmlPagelet("collection1", futureColection1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", futureColection2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", futureColection3.map(x => views.html.product.collection(x)))

    val futureAside = Future(1)
//    val delay4 = 0
//    val futureAside = Promise.timeout(futureAside2, delay4.second).flatMap(x => x)

    val aside = HtmlPagelet("aside", futureAside.map(_ => views.html.partials.aside()))

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3, aside)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3, aside)))
  }




  //---------------------------View product--------------------------------------------------------

  def viewProduct( sub: String, gro: String, pUrl: String) = PjaxAction.async { implicit request =>



    val futureProduct = cache.get[Option[Product]]( pUrl ) match {
      case None => {
        println(s"Not found $pUrl result in cache")
        val futureProduct = DocumentDAO.findOne[Product](cProduct, Json.obj("pUrl" -> pUrl))
//        val delay1 = 1
//        val futureProduct =  Promise.timeout(futureProduct2, delay1.second).flatMap(x => x)

        futureProduct.map{
          list => cache.set(pUrl, list, 30 days)
            cacheList += pUrl
        }
        futureProduct
      }
      case Some(p) => {
        println(s"found $pUrl result")
        Future(p)
      }
    }

    if (request.pjaxEnabled)
      futureProduct.map { p => p match {
        case Some(data) => Ok(views.html.product.view(data))
        case None => Ok("ko ton tai")
      }}
    else {
      val pageletProduct = HtmlPagelet("product" , futureProduct.map{x => x match {
        case Some(data) => views.html.product.view(data)
        case None     => views.html.product.view2()
        }
      })
      val bigPipe = new BigPipe(renderOptions(request), pageletProduct)
      Future(Ok.chunked(views.stream.product.view(bigPipe, pageletProduct)))
    }
  }


  //-----------------View Collection -------------------------------------------------------------
//routes.ProductCtrl.collection(subTypeUrl, groupUrl, _kw, _li, _br, _or, _lt, _ln).url

  def collection(subTypeUrl:String = "", groupUrl: String = "", _kw:String = "", _li:Int = 8,
                 _br:String = "", _or:String = "", _lt:String = "", _ln:String = "" ) =
    PjaxAction.async { implicit request =>

      val futureList = cache.get[List[Product]]("collection" +
            subTypeUrl + groupUrl + _kw + _li + _br + _or + _lt + _ln) match {
        case None => {
          println("Not found collection result in cache")
          val futureList = cProduct.find(Json.obj(
          "subTypeUrl" -> Json.obj("$regex" -> (".*" + subTypeUrl + ".*"), "$options" -> "-i"),
          "groupUrl" -> Json.obj("$regex" -> (".*" + groupUrl + ".*"), "$options" -> "-i"),
          "name" -> Json.obj("$regex" -> (".*" + _kw + ".*"), "$options" -> "-i"),
          "brand" -> Json.obj("$regex" -> (".*" + _br + ".*"), "$options" -> "-i"),
          "origin" -> Json.obj("$regex" -> (".*" + _or + ".*"), "$options" -> "-i"),
          "legType" -> Json.obj("$regex" -> (".*" + _lt + ".*"), "$options" -> "-i"),
          "legNumber" -> Json.obj("$regex" -> (".*" + _ln + ".*"), "$options" -> "-i")
        ))
        .cursor[Product]().collect[List](_li)

          futureList.map{

            list => cache.set("collection" +
                subTypeUrl + groupUrl + _kw + _li + _br + _or + _lt + _ln, list, 30 days)

              cacheList += "collection" +
                  subTypeUrl + groupUrl + _kw + _li + _br + _or + _lt + _ln
          }
          futureList
        }

        case Some(p) => {
          println("found collection result")
          Future(p)
        }
      }


      if (request.pjaxEnabled){
        futureList.map{
          list => Ok(views.html.partials.category(list, subTypeUrl, groupUrl, _kw, _li, _br, _or, _lt, _ln))
        }
      }else{

        val pageletColelction = HtmlPagelet("collection", futureList.map(x => views.html.product.collection(x)))

        val bigPipe = new BigPipe(renderOptions(request), pageletColelction)
        Future.successful(Ok.chunked(views.stream.category(bigPipe, pageletColelction, subTypeUrl, groupUrl, _kw, _li)))
      }
  }


    //----------------------search----------------------------------------------------------------------

  def search(sup:String = "" , sub:String = "", gro: String = "", _kw:String = "", _li:Int = 8) = PjaxAction.async { implicit request =>

    val futureList = cache.get[List[Product]]("search" + sup + sub + gro + _kw + _li) match {
      case None => {
        println("Not found search result in cache")
        val futureList = cProduct.find(Json.obj(
          "subType" -> Json.obj("$regex" -> (".*" + sup + ".*"), "$options" -> "-i"),
          "subType" -> Json.obj("$regex" -> (".*" + sub + ".*"), "$options" -> "-i"),
          "group" -> Json.obj("$regex" -> (".*" + gro + ".*"), "$options" -> "-i"),
          "name" -> Json.obj("$regex" -> (".*" + _kw + ".*"), "$options" -> "-i")))
          .cursor[Product]().collect[List](_li)

        futureList.map{
          list => cache.set("search" + sup + sub + gro + _kw + _li, list, 30 days)
            cacheList += "search" + sup + sub + gro + _kw + _li
        }
        futureList
      }

      case Some(p) => {
        println("found search result")
        Future(p)
      }
    }


    if (request.pjaxEnabled){
      futureList.map{
        list => Ok(views.html.partials.category(list, sub, gro, _kw, _li))
      }
    }else{

      val pageletColelction = HtmlPagelet("collection", futureList.map(x => views.html.product.collection(x)))

      val bigPipe = new BigPipe(renderOptions(request), pageletColelction)
      Future.successful(Ok.chunked(views.stream.category(bigPipe, pageletColelction, sub, gro, _kw, _li)))
    }
  }

  //-------------------  view list products ----------------------------------------------------------

  def listProduct(page: Int, _pp : Int, _n: String, _c:String, _g: String,
                  _min: Int = 0, _max: Int = 100000000) = PjaxAction.async { implicit request =>

    val futureList = cache.get[List[Product]]("listProduct" + page) match {
      case None => {
        println("case 1")

        val futureList = cProduct.find(Json.obj(
        "name" -> Json.obj("$regex" -> (".*" + _n + ".*"), "$options" -> "-i"),
        "code" -> Json.obj("$regex" -> (".*" + _c + ".*"), "$options" -> "-i"),
        "group" -> Json.obj("$regex" -> (".*" + _g + ".*"), "$options" -> "-i"),
        "price" -> Json.obj("$gte" -> _min),
        "price" -> Json.obj("$lte" -> _max)))
        .options(QueryOpts((page-1) * _pp))
        .cursor[Product]().collect[List](_pp)

        futureList.map{
          list => cache.set("listProduct" + page, list, 30 days)
          cacheList += "listProduct" + page
        }
        futureList
      }

      case Some(p) => {
        println("case 2")
        Future(p)
      }
    }

    futureList.map{
      list => Ok(views.html.product.list(list))
    }
  }

  //----------------- Server render for google bot    ----------------------------------------------

  private def renderOptions(request: RequestHeader): PageletRenderOptions = {
    request.getQueryString("server") match {
      case Some("true") =>
      PageletRenderOptions.ServerSide

      case _            => {
        request.headers.get(HeaderNames.USER_AGENT) match {
          case Some(header) if header.contains("GoogleBot") => PageletRenderOptions.ServerSide
          case _ => PageletRenderOptions.ClientSide
        }
      }
    }
  }

  //--------------------------------------------------------------------------------------------------

  def test = cached((request: RequestHeader) => request.toString(), 10){
    Action.async { implicit request =>

      clearCache()

      val command = Aggregate("product", Seq(
        GroupField("subType")("total" -> SumValue(1))
        )
      )
      val result = cProduct.db.command(command)
      //val result = Promise.timeout(result0, 0.second).flatMap(x => x)
      result.map {x => Ok(Json.toJson(x))}
    }
  }

  def test2 = Action.async {implicit request =>
    val futureResult: Future[String] = ws.url("http://localhost:9000/test").get().map {
      response =>
        response.body
    }
    futureResult.map{
      r => Ok(Json.toJson(r))
    }
  }

  def apiListCollection = Action.async {
    val futureList = cProduct.find(Json.obj()).cursor[Product]().collect[List](8)
    futureList.map{
      list => Ok(list.toString())
    }
  }

  def clearCache() = {
    cacheList.toList.foreach(
      key => {
        println("removing:" + key)
        cache.remove(key)
      }
    )
  }

}
