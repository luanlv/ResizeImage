package controllers

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.{JsonPagelet, BigPipe, HtmlPagelet}
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._
import play.api.Configuration

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
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.api.QueryOpts
import reactivemongo.api.indexes.{IndexType, Index}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

import scala.util.Random
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

class ProductCtrl @Inject() (
     val messagesApi: MessagesApi,
     val reactiveMongoApi: ReactiveMongoApi,
     config: Configuration)
    extends Controller with MongoController with ReactiveMongoComponents with Pjax{

  //--------------------    SETUP   ------------------------------------------------------------
  val cProduct = {
    val cProduct = db[JSONCollection]("product")
    cProduct.indexesManager.ensure(
      Index(List("code" -> IndexType.Ascending), unique = true)
    )
    cProduct
  }


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
          lastError => Ok("ok")
        }.recover{
          case e => {
            Ok(views.html.product.create(Product.form.fill(product), e.getMessage()))
          }
        }
      }
    )
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
          ))

        // ok, let's do the update
        cProduct.update(Json.obj("pUrl" -> pUrl), modifier).
            map { _ => Redirect(routes.ProductCtrl.viewEdit(product.pUrl)).flashing("success" -> "update OK!") }
      }
    )
  }

  //-------------------------  Index page   ----------------------------------------------------------

  def index2 = PjaxAction.async { implicit request =>
    val futureColection1 = cProduct.find(Json.obj("supType" -> "pctb"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)
    val futureColection2 = cProduct.find(Json.obj("supType" -> "bdcb"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)

    val futureColection3 = cProduct.find(Json.obj("supType" -> "lkpk"))
        .sort(Json.obj("updateDate" -> -1))
        .cursor[Product]().collect[List](8)

    val delay1 = 0.75
    val delayed1 =  Promise.timeout(futureColection1, delay1.second).flatMap(x => x)

    val delay2 = 0.5
    val delayed2 =  Promise.timeout(futureColection2, delay2.second).flatMap(x => x)

    val delay3 = 0.25
    val delayed3 =  Promise.timeout(futureColection3, delay3.second).flatMap(x => x)


    val pageletColelction1 = HtmlPagelet("collection1", delayed1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", delayed2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", delayed3.map(x => views.html.product.collection(x)))

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3)))
  }

  def index = PjaxAction.async { implicit request =>
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

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3)))
  }



  //---------------------------View product--------------------------------------------------------

  def viewProduct( sub: String, gro: String, pUrl: String) = PjaxAction.async { implicit request =>

    val futureProduct = DocumentDAO.findOne[Product](cProduct, Json.obj("pUrl" -> pUrl))

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
      Future(Ok.chunked(views.stream.view(bigPipe, pageletProduct)))
    }
  }


  //-----------------View Collection -------------------------------------------------------------


  def collection(subTypeUrl:String = "", groupUrl: String = "", _kw:String = "", _li:Int = 8,
                 _br:String = "", _or:String = "", _lt:String = "", _ln:String = "" ) = PjaxAction.async { implicit request =>
    println("br="+_br)
    println("li="+_li)
    println("lt="+_lt)
    println("kw="+_kw)
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

    if (request.pjaxEnabled){
      futureList.map{
        list => Ok(views.html.category(list, subTypeUrl, groupUrl, _kw, _li, _br, _or, _lt, _ln))
      }
    }else{

      val pageletColelction = HtmlPagelet("collection", futureList.map(x => views.html.product.collection(x)))

      val bigPipe = new BigPipe(renderOptions(request), pageletColelction)
      Future.successful(Ok.chunked(views.stream.category(bigPipe, pageletColelction, subTypeUrl, groupUrl, _kw, _li)))
    }
  }

    //----------------------search----------------------------------------------------------------------

  def search(sup:String = "" , sub:String = "", gro: String = "", _kw:String = "", _li:Int = 8) = PjaxAction.async { implicit request =>

    val futureList = cProduct.find(Json.obj(
      "subType" -> Json.obj("$regex" -> (".*" + sup + ".*"), "$options" -> "-i"),
      "subType" -> Json.obj("$regex" -> (".*" + sub + ".*"), "$options" -> "-i"),
      "group" -> Json.obj("$regex" -> (".*" + gro + ".*"), "$options" -> "-i"),
      "name" -> Json.obj("$regex" -> (".*" + _kw + ".*"), "$options" -> "-i")))
      .cursor[Product]().collect[List](_li)

    if (request.pjaxEnabled){
      futureList.map{
        list => Ok(views.html.category(list, sub, gro, _kw, _li))
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
    val futureList = cProduct.find(Json.obj(
      "name" -> Json.obj("$regex" -> (".*" + _n + ".*"), "$options" -> "-i"),
      "code" -> Json.obj("$regex" -> (".*" + _c + ".*"), "$options" -> "-i"),
      "group" -> Json.obj("$regex" -> (".*" + _g + ".*"), "$options" -> "-i"),
      "price" -> Json.obj("$gte" -> _min),
      "price" -> Json.obj("$lte" -> _max)))
      .options(QueryOpts((page-1) * _pp))
      .cursor[Product]().collect[List](_pp)
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
}
