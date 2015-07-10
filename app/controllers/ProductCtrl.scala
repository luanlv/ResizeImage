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

  //-------------------------  Index page   ----------------------------------------------------------

  def index = PjaxAction.async { implicit request =>
    val futureColection1 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "1"), 8)
    val futureColection2 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "2"), 8)
    val futureColection3 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "3"), 8)

    val delay1 = 6
    val delayed1 =  Promise.timeout(futureColection1, delay1.second).flatMap(x => x)

    val delay2 = 4
    val delayed2 =  Promise.timeout(futureColection2, delay2.second).flatMap(x => x)

    val delay3 = 2
    val delayed3 =  Promise.timeout(futureColection3, delay3.second).flatMap(x => x)


    val pageletColelction1 = HtmlPagelet("collection1", delayed1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", delayed2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", delayed3.map(x => views.html.product.collection(x)))

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3)))
  }

  def index2 = PjaxAction.async { implicit request =>
    val futureColection1 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "1"), 8)
    val futureColection2 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "2"), 8)
    val futureColection3 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "3"), 8)

    val pageletColelction1 = HtmlPagelet("collection1", futureColection1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", futureColection2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", futureColection3.map(x => views.html.product.collection(x)))

    val bigPipe = new BigPipe(renderOptions(request), pageletColelction1, pageletColelction2, pageletColelction3)
    Future.successful(Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3)))
  }


  def client = PjaxAction { implicit request =>
    val futureJson1 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "1"), 8).map(x => Json.toJson(x))
    val futureJson2 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "2"), 8).map(x => Json.toJson(x))
    val futureJson3 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "3"), 8).map(x => Json.toJson(x))


    val pagelet1 = JsonPagelet("collection1", futureJson1)
    val pagelet2 = JsonPagelet("collection2", futureJson2)
    val pagelet3 = JsonPagelet("collection3", futureJson3)

    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, pagelet1, pagelet2, pagelet3)
    Ok.chunked(views.stream.clientSideTemplating(bigPipe, pagelet1, pagelet2, pagelet3))
  }

  def clientDelay = PjaxAction { implicit request =>
    val futureJson1 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "1"), 8).map(x => Json.toJson(x))
    val futureJson2 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "2"), 8).map(x => Json.toJson(x))
    val futureJson3 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "3"), 8).map(x => Json.toJson(x))

    val delay1 = 6
    val delayed1 =  Promise.timeout(futureJson1, delay1.second).flatMap(x => x)

    val delay2 = 4
    val delayed2 =  Promise.timeout(futureJson2, delay2.second).flatMap(x => x)

    val delay3 = 2
    val delayed3 =  Promise.timeout(futureJson3, delay3.second).flatMap(x => x)

    val pagelet1 = JsonPagelet("collection1", delayed1)
    val pagelet2 = JsonPagelet("collection2", delayed2)
    val pagelet3 = JsonPagelet("collection3", delayed3)

    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, pagelet1, pagelet2, pagelet3)
    Ok.chunked(views.stream.clientSideTemplating(bigPipe, pagelet1, pagelet2, pagelet3))
  }

  //---------------------------View product--------------------------------------------------------

  def viewProduct(code: String) = PjaxAction.async { implicit request =>

    val futureProduct = DocumentDAO.findOne[Product](cProduct, Json.obj("code" -> code))

    if (request.pjaxEnabled)
      futureProduct.map ( p => Ok(views.html.product.view(p)))
    else {
      val delayed =  Promise.timeout(futureProduct, 1.second).flatMap(x => x)
      val pageletProduct = HtmlPagelet("product" , delayed.map(x => views.html.product.view(x)))
      val bigPipe = new BigPipe(renderOptions(request), pageletProduct)
      Future(Ok.chunked(views.stream.view(bigPipe, pageletProduct)))
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
