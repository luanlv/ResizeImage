package controllers

import javax.inject.Inject
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ScaleMethod.FastScale
import com.sksamuel.scrimage.nio.JpegWriter
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Request}

import play.modules.reactivemongo.JSONFileToSave
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import MongoController.readFileReads
import play.modules.reactivemongo.json._

import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONDocument

import ImplicitBSONHandlers._

class ImageCtrl @Inject() (
    val messagesApi: MessagesApi,
    val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents {

  import java.util.UUID

  private val gridFS = reactiveMongoApi.gridFS

  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  def upload = Action.async(gridFSBodyParser(gridFS)) { request =>
    val uuid = UUID.randomUUID().toString

    val futureImage = request.body.files.head.ref
    val futureUpdate = for {
     image <- futureImage

      updateResult <- {
        gridFS.files.update(
          BSONDocument("_id" -> image.id),
          BSONDocument("$set" ->
              BSONDocument("metadata" -> BSONDocument("UUID" -> uuid, "size" -> "normal"))
          )
        )


        // Create resized image
        val iterator = gridFS
            .enumerate[JSONSerializationPack.Value](image)
            .run(Iteratee.consume[Array[Byte]]())

        iterator.flatMap {
          bytes => {
            val enumerator: Enumerator[Array[Byte]] = Enumerator.outputStream(
              out => {
                implicit val writer = JpegWriter().withProgressive(true)
                Image(bytes).scaleTo(120, 120, FastScale).forWriter(writer).write(out)
              }
            )

            val data = JSONFileToSave(
              filename = image.filename,
              contentType = image.contentType,
              uploadDate = Some(DateTime.now().getMillis),
              metadata =  Json.obj(
                "UUID" -> uuid,
                "size" -> "thumb"
              )
            )
            gridFS.save(enumerator, data).map {
              image => Some(image)
            }
          }
        }

      }
    } yield updateResult

    futureUpdate.map {
      case _ => {
        Ok(views.html.result(uuid))
      }
    }.recover {
      case e => {
        println(e.getMessage())
        InternalServerError(e.getMessage())
      }
    }
  }


  def get(uuid: String, size: String) = Action.async { request =>

    type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]
    val image = gridFS.find[JsObject, JSONReadFile](Json.obj("metadata.UUID" -> uuid, "metadata.size" -> size ))

    request.getQueryString("inline") match {
      case _ => serve[JsString, JSONReadFile](gridFS)(image, CONTENT_DISPOSITION_INLINE)
    }
  }
}
