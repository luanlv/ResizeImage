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
import models._
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
import reactivemongo.api.indexes.IndexType.{Geo2DSpherical, Geo2D}
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

import core.Cdn._



class ProductCtrl @Inject() (
     val messagesApi: MessagesApi,
     val reactiveMongoApi: ReactiveMongoApi,
     ws: WSClient,
     cache: CacheApi,
     cached: Cached,
     config: Configuration)
    extends Controller with MongoController with ReactiveMongoComponents with Pjax{

  //--------------------    SETUP   ------------------------------------------------------------
  val cacheQueriesDay = 7 days
  val cachePage = 5
  val timeCacheRandom = 180 second
  val timeCacheSearch = 60 minute

  val cProduct = {
    val cProduct = db[JSONCollection]("product")
    cProduct.indexesManager.ensure(
      Index(List("core.code" -> IndexType.Ascending, "url.pUrl" -> IndexType.Ascending), unique = true)
    )
    cProduct.indexesManager.ensure(
      Index(List("core.name" -> IndexType.Text))
    )
    cProduct.indexesManager.ensure(
      Index(List("info.brand" -> IndexType.Ascending,
        "info.origin" -> IndexType.Ascending,
        "info.legType" -> IndexType.Ascending,
        "info.legNumber" -> IndexType.Ascending))
    )
    cProduct.indexesManager.ensure(Index(
        List("random_point" -> Geo2DSpherical)
    ))
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
        val r = Random.nextDouble()
        val tmp = product.copy(
          id = product.id.orElse(Some(UUID.randomUUID().toString)),
          random_point =  Some(List(0.5, 0)),
          creationDate = Some(new DateTime()),
          updateDate = Some(new DateTime())
        )
        val f = insert[Product](cProduct, tmp)
        f.map{
          lastError => {
            clearCache(List("search", "supType", "subType", "cachegroup", tmp.url.supType, tmp.url.subType, tmp.url.group, tmp.url.pUrl, tmp.info.group))
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
      clearCache(List("search", "supType", "subType", "cachegroup", "wfilter", "collection-"))
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

//    case class Core(code: String, name: String, price: List[ListPrice])
//
//
//    case class Info(image: List[ImageUrl], unit: String, stock: Int, sold: Int, vote: Int, group: String, brand:String,
//                    origin: String, legType: String, legNumber: String)
//
//    case class Extra(saleOff1: Int, saleOff2: Int, info: String, note: String)
//
//    case class Product(id: Option[String], url: Url, core: Core, info: Info, extra: Extra,
//                       creationDate: Option[DateTime], updateDate: Option[DateTime])

      product => {
        val modifier = Json.obj(
          "$set" -> Json.obj(
            "url" -> Json.obj(
              "supType" -> product.url.supType,
              "subType" -> product.url.subType,
              "pUrl" -> product.url.pUrl,
              "tag" -> Json.toJsFieldJsValueWrapper(product.url.tag)
            ),
            "core" -> Json.obj(
              "code" -> product.core.code,
              "name" -> product.core.name,
              "price" -> Json.toJsFieldJsValueWrapper(product.core.price)
            ),
            "info" -> Json.obj(
              "image" -> Json.toJsFieldJsValueWrapper(product.info.image),
              "unit" -> product.info.unit,
              "stock" -> product.info.stock,
              "sold" -> product.info.sold,
              "vote" -> product.info.group,
              "brand" -> product.info.brand,
              "origin" -> product.info.origin,
              "legType" -> product.info.legType,
              "legNumber" -> product.info.legNumber
            ),
            "extra" -> Json.obj(
              "saleOff1" -> product.extra.saleOff1,
              "saleOff2" -> product.extra.saleOff2,
              "info" -> product.extra.info,
              "note" -> product.extra.note
            ),
            "updateDate" -> BSONDateTime(new DateTime().getMillis)
          )
        )

        // ok, let's do the update
        cProduct.update(Json.obj("pUrl" -> pUrl), modifier).
            map { _ => {
              clearCache(List("search", "supType", "subType", "cachegroup", "wfilter", "collection-"))
                Redirect(routes.ProductCtrl.viewEdit(product.url.pUrl)).flashing("success" -> "update OK!")
              }
            }
      }
    )
  }

  //-------------------------  Index page   ----------------------------------------------------------


  def index = Cached((rh: RequestHeader) => rh.uri, cachePage) { PjaxAction.async { implicit request =>

    val cacheName1 = "phan-cung-thiet-bi"

    val futureCollection1 = cache.get[List[Product]]( cacheName1 ) match {
      case None => {
        println(s"Not found $cacheName1")
        val futureCollection1 = cProduct.find(Json.obj("url.supType" -> "phan-cung-thiet-bi"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](12)

        futureCollection1.map {
          list => cache.set(cacheName1, list, cacheQueriesDay)
            cacheList += cacheName1
        }
        futureCollection1
      }

      case Some(p) => {
        println(s"found $cacheName1")
        Future(p)
      }
    }

    val cacheName2 = "ban-dan-cam-bien"
    val futureCollection2 = cache.get[List[Product]]( cacheName2 ) match {
      case None => {
        println(s"Not found $cacheName2")
        val futureColection2 = cProduct.find(Json.obj("url.supType" -> "ban-dan-cam-bien"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](12)

        futureColection2.map {
          list => cache.set(cacheName2, list, cacheQueriesDay)
            cacheList += cacheName2
        }
        futureColection2
      }
      case Some(p) => {
        println(s"found $cacheName2")
        Future(p)
      }
    }

    val cacheName3 = "lk-khac-phu-kien"
    val futureCollection3 = cache.get[List[Product]]( cacheName3 ) match {
      case None => {
        println(s"Not found $cacheName3")

        val futureCollection3 = cProduct.find(Json.obj("url.supType" -> "lk-khac-phu-kien"))
          .sort(Json.obj("updateDate" -> -1))
          .cursor[Product]().collect[List](12)

        futureCollection3.map {
          list => cache.set(cacheName3, list, cacheQueriesDay)
          cacheList += cacheName3
        }
        futureCollection3
      }

      case Some(p) => {
        println(s"found $cacheName3")
        Future(p)
      }
    }


    val cacheName4 = "saleOff"
    val futureSaleOff = cache.get[List[Product]]( cacheName4 ) match {
      case None => {
        println(s"Not found $cacheName4")

        val futureSaleOff = cProduct.find(Json.obj("extra.saleOff1" -> Json.obj("$gt" -> 0)))
          .sort(Json.obj("extra.saleOff1" -> -1))
          .cursor[Product]().collect[List](20)

        futureSaleOff.map {
          list => cache.set(cacheName4, list, cacheQueriesDay)
            cacheList += cacheName4
        }
        futureSaleOff
      }

      case Some(p) => {
        println(s"found $cacheName4")
        Future(p)
      }
    }


    val supType = getSupType()

    val subType = getSubType()

    val group = getGroup()


    val futureResult = for {
      futureCollection1 <- futureCollection1
      futureCollection2 <- futureCollection2
      futureCollection3 <- futureCollection3
      supType <- supType
      subType <- subType
      group <- group
      futureSaleOff <- futureSaleOff
    } yield (futureCollection1, futureCollection2, futureCollection3, supType, subType, group, futureSaleOff)



    futureResult.map{
      result => {
        if (request.pjaxEnabled){
//          Ok(views.html.partials.index(result._1, result._2, result._3, result._4, result._5, result._6))
          Ok("")
        }else {
          val pageletColelction1 = HtmlPagelet("collection1", Future(views.html.product.collection(assetCDN, map, result._1)))
          val pageletColelction2 = HtmlPagelet("collection2", Future(views.html.product.collection(assetCDN, map, result._2)))
          val pageletColelction3 = HtmlPagelet("collection3", Future(views.html.product.collection(assetCDN, map, result._3)))

          val aside = HtmlPagelet("aside", Future(views.html.partials.asideIndex(result._4, result._5, result._6)))

          val saleOff = HtmlPagelet("saleoff", Future(views.html.product.saleoff(assetCDN, map, "Khuyến mãi", result._7)))

          val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3, aside, saleOff)
          Ok.chunked(views.stream.index(assetCDN, map, bigPipe, pageletColelction1, pageletColelction2, pageletColelction3, aside, saleOff))
        }
      }
    }
  }}


  //---------------------------View product--------------------------------------------------------

  def viewProduct( sub: String, gro: String, pUrl: String, _sb: String = "", _v: String = "", _kw: String = "", _li: Int = 8) =
    Cached((rh: RequestHeader) => rh.uri + pUrl, cachePage)  { PjaxAction.async { implicit request =>


    val futureProduct = cache.get[Option[Product]]( pUrl ) match {
      case None => {
        println(s"Not found $pUrl")
        val futureProduct = DocumentDAO.findOne[Product](cProduct, Json.obj("url.pUrl" -> pUrl))
//        val delay1 = 1
//        val futureProduct =  Promise.timeout(futureProduct2, delay1.second).flatMap(x => x)

        futureProduct.map{
          list => cache.set(pUrl, list, cacheQueriesDay)
            cacheList += pUrl
        }
        futureProduct
      }
      case Some(p) => {
        println(s"found $pUrl")
        Future(p)
      }
    }

    val cacheName = "relative" + gro

    val futureRelative = cache.get[List[Product]] (cacheName ) match {
      case None => {
        println(s"not found $cacheName")
        val futureRelative = getRandomJson(sub, gro, 10)
        futureRelative.map {
          list => cache.set(cacheName, list, cacheQueriesDay)
            cacheList += cacheName
        }
        futureRelative
      }
      case Some(p) => {
        println(s"found $cacheName")
        Future(p)
      }
    }

    val FutureRelativeProduct = getRandomJson(sub, gro, 14)


    val supType = getSupType()

    val subType = getSubType()

    val group = getGroup()

    val futureResult = for {
      futureProduct <- futureProduct
      supType <- supType
      subType <- subType
      group <- group
      futureRelative <- futureRelative
    } yield (futureProduct, supType, subType, group, futureRelative)

    futureResult.map{
      result => {
        if (request.pjaxEnabled)
//          result._1 match {
//            case Some(data) => Ok(views.html.product.view(data, result._2, result._3, result._4, _sb, _v, _li))
//            case None => Ok("ko ton tai")
//          }
          Ok("")
        else {
          val v = result._1 match {
            case Some(data) => Future(views.html.product.viewWithoutAside(assetCDN, map, data, result._2, result._3, result._4, _sb, _v, _li))
            case _ => Future(views.html.product.view2())
          }
          val pageletProduct = HtmlPagelet("product" , v)



          val pageletAside = HtmlPagelet("aside", Future(views.html.partials.viewAside(result._2,result._3, result._4, sub, gro, _sb, _v, _li)))

          val saleRelative = HtmlPagelet("relative", Future(views.html.product.saleoff(assetCDN, map, "Sản phẩm tương tự", result._5)))

          val bigPipe = new BigPipe(renderOptions(request), pageletProduct, pageletAside, saleRelative)
          val subType = result._1 match {
            case Some(data) => data.url.subType
            case _ => ""
          }
          Ok.chunked(views.stream.product.view(assetCDN, map, bigPipe, result._2, result._3, result._4,
            pageletProduct, pageletAside, saleRelative,  subType, gro , pUrl, _sb, _v, _kw, _li))
        }
      }
    }
  }}


  //-----------------View Collection -------------------------------------------------------------
//routes.ProductCtrl.collection(subTypeUrl, groupUrl, _kw, _li, _br, _or, _lt, _ln).url

  def collection(subTypeUrl:String = "", groupUrl: String = "",
                 _page: Int = 1, _sb: String = "", _kw:String = "", _li:Int = 12, _v:String = "",
                 _br:String = "", _or:String = "", _lt:String = "", _ln:String = "" ,
                 _min:Int = 0, _max: Int = 500000000) =
    Cached((rh: RequestHeader) => rh.uri + groupUrl, cachePage) {PjaxAction.async { implicit request =>

      val cacheName = "collection-" + subTypeUrl +  groupUrl + _kw + _li + _br + _or + _lt + _ln + _min + _max

      val futureList = cache.get[List[Product]](cacheName) match {
        case None => {
          println(s"Not found $cacheName ")
          val newKw = vnSearch(_kw)

          val jsSubType =
            if(subTypeUrl != "")
              Json.obj("$eq" -> subTypeUrl)
            else
              Json.obj("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")

          val jsGroup =
            if(groupUrl != "list"){
              Json.obj("$eq" -> groupUrl)
            } else {
              Json.obj("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
            }
          val jsBrand =
            if(_br == ""){
              Json.obj("$regex" -> (".*" + _br + ".*"), "$options" -> "-i")
            } else {
              Json.obj("$eq" -> _br)
            }
          val jsOrigin =
            if(_or == ""){
              Json.obj("$regex" -> (".*" + _or + ".*"), "$options" -> "-i")
            } else {
              Json.obj("$eq" -> _or)
            }
          val jsLegType =
            if(_lt == "") {
              Json.obj("$regex" -> (".*" + _lt + ".*"), "$options" -> "-i")
            } else {
              Json.obj("$eq" -> _lt)
            }
          val jsLegNumber =
            if(_ln == "") {
              Json.obj("$regex" -> (".*" + _ln + ".*"), "$options" -> "-i")
            } else {
              Json.obj("$eq" -> _ln)
            }

          val jsSort = if(_sb == "") {
            Json.obj("updateDate" -> -1)
          } else {
            Json.obj("updateDate" -> -1)
          }

          val futureList = cProduct.find(Json.obj(
          "url.subType" -> jsSubType,
          "url.group" -> jsGroup,
          "$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + newKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + newKw + ".*"), "$options" -> "-i"))),
          "info.brand" -> jsBrand,
          "info.origin" -> jsOrigin,
          "info.legType" -> jsLegType,
          "info.legNumber" -> jsLegNumber,
          "core.price.0.price" -> Json.obj("$gte" -> _min, "$lte" -> _max)
        ))
        .sort(jsSort)
        .options(QueryOpts((_page - 1) * _li))
        .cursor[Product]().collect[List](_li)

          futureList.map{
            list => {
              cache.set(cacheName, list, cacheQueriesDay)
              cacheList += cacheName
              list
            }
          }
          futureList
        }

        case Some(p) => {
          println(s"found $cacheName ")
          Future(p)
        }
      }

      val cacheName4 = "saleOff"
      val futureSaleOff = cache.get[List[Product]]( cacheName4 ) match {
        case None => {
          println(s"Not found $cacheName4")

          val futureSaleOff = cProduct.find(Json.obj("extra.saleOff1" -> Json.obj("$gt" -> 0)))
          .sort(Json.obj("extra.saleOff1" -> -1))
          .cursor[Product]().collect[List](20)

          futureSaleOff.map {
            list => cache.set(cacheName4, list, cacheQueriesDay)
              cacheList += cacheName4
          }
          futureSaleOff
        }

        case Some(p) => {
          println(s"found $cacheName4")
          Future(p)
        }
      }

      val tmpGroupUrl =
        if(groupUrl == "list")
          ""
        else
          groupUrl

      println(subTypeUrl + "<============")
      val supType = getSupType()
      val subType = getSubType()
      val group = getGroup()
      val brand = getBrand(subTypeUrl, tmpGroupUrl, _kw, _or, _lt, _ln, _min, _max)
      val origin = getOrigin(subTypeUrl, tmpGroupUrl, _kw, _br, _lt, _ln, _min, _max)
      val legType = getLegType(subTypeUrl, tmpGroupUrl, _kw, _br, _or, _ln, _min, _max)
      val legNumber = getLegNumber(subTypeUrl, tmpGroupUrl, _kw, _br, _or, _lt, _min, _max)

      val futureResult = for {
        futureList <- futureList
        supType <- supType
        subType <- subType
        group <- group
        brand <- brand
        origin <- origin
        legType <- legType
        legNumber <- legNumber
        futureSaleOff <- futureSaleOff
      } yield (futureList, supType, subType, group, brand, origin, legType, legNumber, futureSaleOff)

      futureResult.map {
        result => {
          if (request.pjaxEnabled) {
            //        futureResult.map{
            //          result => Ok(views.html.partials.category(result._1, result._2, result._3, result._4,
            //            result._5, result._6, result._7, result._8,
            //            subTypeUrl, groupUrl, _page, _sb, _kw, _li, _v, _br, _or, _lt, _ln, _min, _max))
            //        }
            Ok("")
          } else {

            val pageletColelction = HtmlPagelet("collection", Future(views.html.product.collection(assetCDN, map, result._1, _sb, _v, _kw, _li, subTypeUrl, groupUrl)))

            val pageletAside = HtmlPagelet("aside",
              Future(views.html.partials.aside(result._2, result._3, result._4,
                result._5, result._6, result._7, result._8,
                subTypeUrl, groupUrl, _sb, _kw, _li, _v, _br, _or, _lt, _ln, _min, _max))

            )

            val saleOff = HtmlPagelet("saleoff", Future(views.html.product.saleoff(assetCDN, map, "Khuyến mãi", result._9)))

            val bigPipe = new BigPipe(renderOptions(request), pageletColelction, pageletAside, saleOff)

            Ok.chunked(views.stream.category(assetCDN, map, bigPipe, result._2, result._3, result._4,
              result._5, result._6, result._7, result._8,
              pageletColelction, pageletAside, saleOff, subTypeUrl, groupUrl, _page, _sb, _kw, _li, _v))
          }
        }
      }
   }
  }

  //-------------------  search --------------------------------------------------------------------

  def search(_sub: String = "", _kw: String = "", _page: Int = 1, _sb: String = "", _li: Int = 12, _v: String = "",
             _br:String = "", _or:String = "", _lt:String = "", _ln:String = "" ,
             _min:Int = 0, _max: Int = 500000000) =

    PjaxAction.async {implicit request =>


      val newKw = vnSearch(_kw)

      val jsSubType =
        if(_sub != "")
          Json.obj("$eq" -> _sub)
        else
          Json.obj("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")

      val jsLegType =
        if(_lt == "") {
          Json.obj("$regex" -> (".*" + _lt + ".*"), "$options" -> "-i")
        } else {
          Json.obj("$eq" -> _lt)
        }
      val jsLegNumber =
        if(_ln == "") {
          Json.obj("$regex" -> (".*" + _ln + ".*"), "$options" -> "-i")
        } else {
          Json.obj("$eq" -> _ln)
        }
      val jsSort = if(_sb == "") {
        Json.obj("updateDate" -> -1)
      } else {
        Json.obj("updateDate" -> -1)
      }

      val futureSearch = cProduct.find(Json.obj(
        "url.subType" -> jsSubType,
        "$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + newKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + newKw + ".*"), "$options" -> "-i"))),
        "info.legType" -> jsLegType,
        "info.legNumber" -> jsLegNumber,
        "core.price.0.price" -> Json.obj("$gte" -> _min, "$lte" -> _max)
      ))
      .sort(jsSort)
      .options(QueryOpts((_page - 1) * _li))
      .cursor[Product]().collect[List](_li)

      val cacheName4 = "saleOff"
      val futureSaleOff = cache.get[List[Product]]( cacheName4 ) match {
        case None => {
          println(s"Not found $cacheName4")

          val futureSaleOff = cProduct.find(Json.obj("extra.saleOff1" -> Json.obj("$gt" -> 0)))
          .sort(Json.obj("extra.saleOff1" -> -1))
          .cursor[Product]().collect[List](20)

          futureSaleOff.map {
            list => cache.set(cacheName4, list, cacheQueriesDay)
              cacheList += cacheName4
          }
          futureSaleOff
        }

        case Some(p) => {
          println(s"found $cacheName4")
          Future(p)
        }
      }


      val supType = getSupType(_kw)
      val subType = getSubType(_kw)
      val group = getGroup(_kw)
      val brand = getBrand(_sub, "", _kw, _or, _lt, _ln, _min, _max)
      val origin = getOrigin(_sub, "", _kw, _br, _lt, _ln, _min, _max)
      val legType = getLegType(_sub, "", _kw, _br, _or, _ln, _min, _max)
      val legNumber = getLegNumber(_sub, "", _kw, _br, _or, _lt, _min, _max)

      val futureResult = for {
        futureSearch <- futureSearch
        supType <- supType
        subType <- subType
        group <- group
        brand <- brand
        origin <- origin
        legType <- legType
        legNumber <- legNumber
        futureSaleOff <- futureSaleOff
      } yield (futureSearch, supType, subType, group, brand, origin, legType, legNumber, futureSaleOff)

      futureResult.map {
        result => {
          val pageletColelction = HtmlPagelet("collection", Future(views.html.product.search(assetCDN, map, result._1, _sb, _v, _kw, _li, _sub)))

          val pageletAside = HtmlPagelet("aside",
            Future(views.html.partials.aside(result._2, result._3, result._4,
              result._5, result._6, result._7, result._8,
              _sub, "list", _sb, _kw, _li, _v, _br, _or, _lt, _ln, _min, _max))

          )

          val saleOff = HtmlPagelet("saleoff", Future(views.html.product.saleoff(assetCDN, map, "Khuyến mãi", result._9)))

          val bigPipe = new BigPipe(renderOptions(request), pageletColelction, pageletAside, saleOff)

          Ok.chunked(views.stream.category(assetCDN, map, bigPipe, result._2, result._3, result._4,
            result._5, result._6, result._7, result._8,
            pageletColelction, pageletAside, saleOff, _sub, "", _page, _sb, _kw, _li, _v))
        }
      }
    }

  //-------------------  view list products --------------------------------------------------------

  def listProduct(page: Int, _pp : Int, _n: String, _c:String, _g: String,
                  _min: Int = 0, _max: Int = 100000000) = PjaxAction.async { implicit request =>

    val futureList = cache.get[List[Product]]("listProduct" + page) match {
      case None => {
        println("case 1")

        val futureList = cProduct.find(Json.obj(
        "core.name" -> Json.obj("$regex" -> (".*" + _n + ".*"), "$options" -> "-i"),
        "core.code" -> Json.obj("$regex" -> (".*" + _c + ".*"), "$options" -> "-i"),
        "info.group" -> Json.obj("$regex" -> (".*" + _g + ".*"), "$options" -> "-i"),
        "core.price.0.price" -> Json.obj("$gte" -> _min, "$lte" -> _max)))
        .options(QueryOpts((page-1) * _pp))
        .cursor[Product]().collect[List](_pp)

        futureList.map{
          list => cache.set("listProduct" + page, list, cacheQueriesDay)
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



  //--------------------------------------------------------------------------------------------------

  def test = cached((request: RequestHeader) => request.uri, 10){
    val rand = Random.nextDouble()
    Action.async { implicit request =>
      val result = getRandomJson("", "raspberry-pi", 1)
      result.map {
        data => Ok(Json.toJson(data))
      }
    }

  }

  def clearAllCache = Action.async {implicit request =>
    clearCache()
    Future(Ok("Clear all cache ok"))
  }

  def apiListCollection = Action.async {
    val futureList = cProduct.find(Json.obj()).cursor[Product]().collect[List](8)
    futureList.map{
      list => Ok(list.toString())
    }
  }

  def clearCache(list: List[String] = List("")) = {
    if(!list.isEmpty){
      list.foreach {
        ele => {
          val pattern = s"$ele".r
          cacheList.toList.foreach {
            s => {
              if(!pattern.findAllIn(s).isEmpty){
                println("removing:" + s)
                cacheList.-(s)
                cache.remove(s)
              }
            }
          }
        }
      }
    }
  }

  def getRandomJson(subTypeUrl: String, groupUrl: String, limit: Int) = {
    val cacheName = "R-a-n-d-o-m" + subTypeUrl + groupUrl + limit
    val data = cache.get[List[Product]] ( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val rand1 = Random.nextDouble()
        val rand2 = Random.nextDouble()
        val jsSubType =
          if(subTypeUrl != "")
            Json.obj("$eq" -> subTypeUrl)
          else
            Json.obj("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
        val jsGroupUrl =
          if(groupUrl != "")
            Json.obj("$eq" -> groupUrl)
          else
            Json.obj("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")

        val result = cProduct.find(Json.obj(

            "random_point" -> Json.obj("$nearSphere" -> Json.arr(rand1, rand2))
          )).cursor[Product]().collect[List](limit)

        result.map {
          list =>
            cache.set(cacheName, list, timeCacheRandom)
            cacheList += cacheName
            list
        }
        result
      }

      case Some(p) =>{
        println(s"Found $cacheName")
        Future(p)
      }
    }
    data
  }

  def getGroup(keyword: String = "") = {
    val cacheName = "cachegroup"+keyword
    val group = cache.get[GroupP]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val command = Aggregate("product", Seq(
            Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
              Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
            GroupField("info.group")("total" -> SumValue(1))
          )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map (x => x.toString().toInt)

            def getValue(st: String): Int = {
              val index = listId.indexOf(st)
              if(index != -1) listValue(index) else 0
            }

            val s111 = getValue("Universal Programmer") +
                getValue("USB Programmer")

            val s112 = getValue("Firmware Master Chip") +
                getValue("ISP Programmer") +
                getValue("ISP Programmer / Emulator") +
                getValue("ISP/Parallel Programmer Mode") +
                getValue("Parallel Programmer mode")

            val s121 = getValue("mini PC") +
                getValue("Raspberry Pi Case")


            val s122 = getValue("Arduino") +
                getValue("Arduino Shield")


            val s211 = getValue("Accelerometer and Gyro Breakout") +
                getValue("Gyroscope sensor")

            val s212 = getValue("Light Sensor") +
                getValue("Sensors")

            val s221 = getValue("Memory")

            val s222 = getValue("Memory-I2C Serial EEPROM")

            val s311 = getValue("LED 1W")

            val s312 = getValue("LEDs")

            val s321 = getValue("Graphic LEDs")

            val s322 = getValue("Graphic LCDs") +
                getValue("LCDs")

            GroupP(s111, s112, s121, s122, s211, s212, s221, s222, s311, s312, s321,  s322)
          }
        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    group
  }

  def getSupType(keyword: String = "") = {
    val cacheName = "supType" + keyword
    val supType = cache.get[SupType]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")

        val vnKw = vnSearch(keyword)

        val command = Aggregate("product", Seq(
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          GroupField("info.supType")("total" -> SumValue(1))
        )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map (x => x.toString().toInt)
            def getValue(st: String): Int = {
              val index = listId.indexOf(st)
              if(index != -1) listValue(index) else 0
            }
            val s1 = getValue("Phần cứng, Thiết bị")
            val s2 = getValue("LK Bán dẫn & Cảm biến")
            val s3 = getValue("LK Khác và Phụ kiện")
            SupType(s1, s2, s3)
          }
        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    supType
  }

  def getSubType(keyword: String = "") = {
    val cacheName = "subType" + keyword
    val subType = cache.get[SubType]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val command = Aggregate("product", Seq(
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          GroupField("info.subType")("total" -> SumValue(1))
        )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map (x => x.toString().toInt)

            def getValue(st: String): Int = {
              val index = listId.indexOf(st)
              if(index != -1) listValue(index) else 0
            }

            val s11 = getValue("Máy Nạp & Adapter")
            val s12 = getValue("Board Phát triển")
            val s21 = getValue("Sensors, Transducers")
            val s22 = getValue("Memory ICs")
            val s31 = getValue("LEDs")
            val s32 = getValue("LCDs Display")
            SubType(s11, s12, s21, s22, s31, s32)
          }
        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    subType
  }

  def getBrand(subTypeUrl: String, group: String, keyword:String = "", origin: String = "", legType: String = "",
               legNumber: String = "", minPrice: Int = 0, maxPrice: Int = 500000000) = {
//    val cacheName = "filter" + group + brand + origin + legType + legNumber + minPrice + maxPrice
    val cacheName = "f-i-l-t-e-r" + subTypeUrl + group + keyword + "brand" + origin + legType + legNumber + minPrice + maxPrice


    val data = cache.get[Brand]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val bsSubTypeUrl =
          if(subTypeUrl == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> subTypeUrl)
          }
        val bsGroup =
          if(group == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> group)
          }
        val bsLegType =
          if(legType == "") {
            BSONDocument("$regex" -> (".*" + legType + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legType)
          }
        val bsLegNumber =
          if(legNumber == "") {
            BSONDocument("$regex" -> (".*" + legNumber + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legNumber)
          }
        val command = Aggregate("product", Seq(
          Match(BSONDocument("url.subType" -> bsSubTypeUrl)),
          Match(BSONDocument("url.group" -> bsGroup)),
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          Match(BSONDocument("info.origin" -> BSONDocument("$regex" -> (".*" + origin + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.legType" -> bsLegType)),
          Match(BSONDocument("info.legNumber" -> bsLegNumber)),
          Match(BSONDocument("core.price.0.price" -> BSONDocument("$gte" -> minPrice, "$lte" -> maxPrice))),
          GroupField("info.brand")("total" -> SumValue(1))
        )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map(x => x.toString().toInt)

              def getValue(st: String): Int = {
                val index = listId.indexOf(st)
                if (index != -1) listValue(index) else 0
              }

              val s1 = getValue("Other")
              val s2 = getValue("Xeltek")
              val s3 = getValue("ATMEL")
              val s4 = getValue("ELNEC")
              val s5 = getValue("TOP")
              val s6 = getValue("SOFI-TECH")
              val s7 = getValue("Raspberry Pi")
              val s8 = getValue("ST")
              val s9 = getValue("InvenSense")
              val s10 = getValue("Eagle Power")
              val s11 = getValue("VISHAY")
              val s12 = getValue("Harvatek")
              val s13 = getValue("Andorin")
              val s14 = getValue("Sharp")
              Brand(s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14)
          }

        }

        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    data
  }

  def getOrigin(subTypeUrl: String, group: String, keyword: String = "", brand: String = "", legType: String = "",
                 legNumber: String = "", minPrice: Int = 0, maxPrice: Int = 500000000) = {

    //    val cacheName = "filter" + group + brand + origin + legType + legNumber + minPrice + maxPrice
    val cacheName = "f-i-l-t-e-r" + subTypeUrl + group + keyword + brand + "origin" + legType + legNumber + minPrice + maxPrice

    val data = cache.get[Origin]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val bsSubTypeUrl =
          if(subTypeUrl == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> subTypeUrl)
          }
        val bsGroup =
          if(group == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> group)
          }
        val bsLegType =
          if(legType == "") {
            BSONDocument("$regex" -> (".*" + legType + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legType)
          }
        val bsLegNumber =
          if(legNumber == "") {
            BSONDocument("$regex" -> (".*" + legNumber + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legNumber)
          }
        val command = Aggregate("product", Seq(
          Match(BSONDocument("url.subType" -> bsSubTypeUrl)),
          Match(BSONDocument("url.group" -> bsGroup)),
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          Match(BSONDocument("info.brand" -> BSONDocument("$regex" -> (".*" + brand + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.legType" -> bsLegType)),
          Match(BSONDocument("info.legNumber" -> bsLegNumber)),
          Match(BSONDocument("core.price.0.price" -> BSONDocument("$gte" -> minPrice, "$lte" -> maxPrice))),
          GroupField("info.origin")("total" -> SumValue(1))
        )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map(x => x.toString().toInt)

            def getValue(st: String): Int = {
              val index = listId.indexOf(st)
              if (index != -1) listValue(index) else 0
            }

            val s1 = getValue("Việt Nam")
            val s2 = getValue("Trung Quốc")
            val s3 = getValue("Chính hãng")
            val s4 = getValue("Taiwan")
            val s5 = getValue("UK")

            Origin(s1,s2,s3,s4,s5)
          }

        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    data
  }

  def getLegType( subTypeUrl: String, group: String, keyword: String = "", brand: String = "", origin: String = "",
                  legNumber: String = "", minPrice: Int = 0, maxPrice: Int = 500000000) = {
    //    val cacheName = "filter" + group + brand + origin + legType + legNumber + minPrice + maxPrice
    val cacheName = "f-i-l-t-e-r" + subTypeUrl + group + keyword + brand + origin + "legType" + legNumber + minPrice + maxPrice

    val data = cache.get[LegType]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val bsSubTypeUrl =
          if(subTypeUrl == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> subTypeUrl)
          }
        val bsGroup =
          if(group == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> group)
          }
        val bsLegNumber =
          if(legNumber == "") {
            BSONDocument("$regex" -> (".*" + legNumber + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legNumber)
          }

        val command = Aggregate("product", Seq(
          Match(BSONDocument("url.subType" -> bsSubTypeUrl)),
          Match(BSONDocument("url.group" -> bsGroup)),
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          Match(BSONDocument("info.brand" -> BSONDocument("$regex" -> (".*" + brand + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.origin" -> BSONDocument("$regex" -> (".*" + origin + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.legNumber" -> bsLegNumber)),
          Match(BSONDocument("core.price.0.price" -> BSONDocument("$gte" -> minPrice, "$lte" -> maxPrice))),
          GroupField("info.legType")("total" -> SumValue(1))
          )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map(x => x.toString().toInt)

            def getValue(st: String): Int ={
              val index = listId.indexOf(st)
              if (index != -1) listValue(index) else 0
            }

            val s1 = getValue("NOPE")
            val s2 = getValue("DIP")
            val s3 = getValue("SIP")
            val s4 = getValue("LQFP")
            val s5 = getValue("PQFP")
            val s6 = getValue("QFN")
            val s7 = getValue("SOIC")
            val s8 = getValue("TQFP")
            val s9 = getValue("T-1 3/4")
            val s10 = getValue("SMD")
            val s11 = getValue("SMD0805")

            LegType(s1,s2,s3,s4,s5, s6, s7, s8, s9, s10, s11)
          }

        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    data
  }

  def getLegNumber(subTypeUrl: String, group: String, keyword: String = "", brand: String = "", origin: String = "",
                   legType: String = "", minPrice: Int = 0, maxPrice: Int = 500000000) = {
    //    val cacheName = "filter" + group + brand + origin + legType + legNumber + minPrice + maxPrice
    val cacheName = "f-i-l-t-e-r" + subTypeUrl + group + keyword + brand + origin + legType + "legNumber" + minPrice + maxPrice

    val data = cache.get[LegNumber]( cacheName ) match {
      case None => {
        println(s"Not found $cacheName")
        val vnKw = vnSearch(keyword)
        val bsSubTypeUrl =
          if(subTypeUrl == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> subTypeUrl)
          }
        val bsGroup =
          if(group == ""){
            BSONDocument("$regex" -> (".*" + "" + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> group)
          }
        val bsLegType =
          if(legType == "") {
            BSONDocument("$regex" -> (".*" + legType + ".*"), "$options" -> "-i")
          } else {
            BSONDocument("$eq" -> legType)
          }

        val command = Aggregate("product", Seq(
          Match(BSONDocument("url.subType" -> bsSubTypeUrl)),
          Match(BSONDocument("url.group" -> bsGroup)),
          Match(BSONDocument("$or" -> Json.arr(Json.obj("core.name" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i")),
            Json.obj("core.code" -> Json.obj("$regex" -> (".*" + vnKw + ".*"), "$options" -> "-i"))))),
          Match(BSONDocument("info.brand" -> BSONDocument("$regex" -> (".*" + brand + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.origin" -> BSONDocument("$regex" -> (".*" + origin + ".*"), "$options" -> "-i"))),
          Match(BSONDocument("info.legType" -> bsLegType)),
          Match(BSONDocument("core.price.0.price" -> BSONDocument("$gte" -> minPrice, "$lte" -> maxPrice))),
          GroupField("info.legNumber")("total" -> SumValue(1))
          )
        )

        val result = cProduct.db.command(command).map(x => Json.toJson(x)).map{
          jsvalue => {
            val listId = jsvalue.\\("_id").toList.map(x => {
              val y = x.toString()
              y.substring(1, y.length - 1)
            }
            )

            val listValue = jsvalue.\\("total").map(x => x.toString().toInt)

            def getValue(st: String): Int = {
              val index = listId.indexOf(st)
              if (index != -1) listValue(index) else 0
            }

            val s1 = getValue("NOPE")
            val s2 = getValue("208")
            val s3 = getValue("100")
            val s4 = getValue("64")
            val s5 = getValue("44")
            val s6 = getValue("40")
            val s7 = getValue("32")
            val s8 = getValue("28")
            val s9 = getValue("24")
            val s10 = getValue("20")
            val s11 = getValue("16")
            val s12 = getValue("14")
            val s13 = getValue("8")
            val s14 = getValue("4")
            val s15 = getValue("2")
            LegNumber(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15)
          }

        }
        result.map {
          list =>{
            val cacheTime =
              if(keyword == "") cacheQueriesDay else timeCacheSearch
            cache.set(cacheName, list, cacheTime)
            cacheList += cacheName
          }
        }
        result
      }
      case Some(p) => {
        println(s"Found $cacheName")
        Future(p)
      }
    }
    data
  }

  def vnSearch(s: String) = {
    println(s)
    val tmp = s.toLowerCase
        .replaceAll("a", "[a|á|à|ả|ã|ạ|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ]")
        .replaceAll("e", "[e|é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ]")
        .replaceAll("i", "[i|í|ì|ỉ|ĩ|ị]")
        .replaceAll("o", "[o|ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ]")
        .replaceAll("u", "[u|ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự]")
        .replaceAll("y", "[y|ý|ỳ|ỷ|ỹ|ỵ]")
        .replaceAll("d", "[d|đ]")
    val arrayWords = tmp.split(" +")
    if(arrayWords.length == 1)
      arrayWords.head
    else
      "[" + arrayWords.mkString("|") + "]"
  }

  def map(st: String) = {
    var listMap =
      Map("may-nap-adapter" -> "Máy Nạp & Adapter",
          "board-phat-trien" -> "Board Phát triển",
          "sensor-transducer" -> "Sensors, Transducers",
          "memory-ic" -> "Memory ICs",
          "led" -> "LEDs",
          "lcd-display" -> "LCDs Display"
      )

    listMap.get(st).getOrElse("Error")
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

}
