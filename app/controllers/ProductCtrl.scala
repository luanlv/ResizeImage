package controllers

import com.ybrikman.ping.javaapi.bigpipe.PageletRenderOptions
import com.ybrikman.ping.scalaapi.bigpipe.{BigPipe, HtmlPagelet}
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamImplicits._

import java.util.UUID
import javax.inject.Inject
import core.dao.DocumentDAO
import core.dao.DocumentDAO._
import models.Product
import org.joda.time.DateTime
import pjax.Pjax
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

class ProductCtrl @Inject() (
     val messagesApi: MessagesApi,
     val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents with Pjax{

  //--------------------    COLLECTION    ------------------------------------------------------------
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
          updateDate = Some(new DateTime()))
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

  def index = PjaxAction { implicit request =>
    val futureColection1 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "1"), 1)
    val futureColection2 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "2"), 1)
    val futureColection3 = DocumentDAO.find[Product](cProduct, Json.obj("group" -> "3"), 1)

    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    import scala.concurrent.duration._

    val delay1 = 2
    val delayed1 =  Promise.timeout(futureColection1, delay1.second).flatMap(x => x)

    val delay2 = 1
    val delayed2 =  Promise.timeout(futureColection2, delay2.second).flatMap(x => x)

    val delay3 = 0
    val delayed3 =  Promise.timeout(futureColection3, delay3.second).flatMap(x => x)


    val pageletColelction1 = HtmlPagelet("collection1", delayed1.map(x => views.html.product.collection(x)))
    val pageletColelction2 = HtmlPagelet("collection2", delayed2.map(x => views.html.product.collection(x)))
    val pageletColelction3 = HtmlPagelet("collection3", delayed3.map(x => views.html.product.collection(x)))

    val bigPipe = new BigPipe(PageletRenderOptions.ClientSide, pageletColelction1, pageletColelction2, pageletColelction3)
    Ok.chunked(views.stream.index(bigPipe, pageletColelction1, pageletColelction2, pageletColelction3))
  }

  //---------------------------View product--------------------------------------------------------

  def viewProduct(code: String) = PjaxAction.async { implicit request =>
    val title = code

    val futureProduct = DocumentDAO.findOne[Product](cProduct, Json.obj("code" -> code))
    futureProduct map {
      p => p match {
        case Some(product) => {
          val view = views.html.product.view(product)
          if (request.pjaxEnabled){
            println("case 1")
            Ok(view)
          }else{
            println("case 2")
            Ok(views.html.layout(title)(view))
          }
        }
        case None => Ok("not found")
      }
    }
  }

  //--------------------------------------------------------------------------------------------------

  //--------------------------------------------------------------------------------------------------
}
