package controllers

import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Request}
import java.util.UUID
import scala.concurrent.Future

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.JSONFileToSave
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.modules.reactivemongo.json._

import reactivemongo.api.{DB, BSONSerializationPack}
import reactivemongo.api.gridfs.{GridFS, FileToSave, DefaultFileToSave, ReadFile}
import reactivemongo.bson.{BSONString, BSONObjectID, BSONValue, BSONDocument}

import com.sksamuel.scrimage.{Format, Image}

import ImplicitBSONHandlers._

class ImageCtrl @Inject() (
  val messagesApi: MessagesApi,
  val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents {

  import java.util.UUID

  import MongoController.readFileReads


  private val gridFS = reactiveMongoApi.gridFS


  private val gridFs = {
    val theDB = db

    GridFS[BSONSerializationPack.type](DB(theDB.name, theDB.connection))
  }

  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  // list all articles and sort them
  def upload = Action.async(gridFSBodyParser(gridFS)) { request =>
    // here is the future file!

    val futureFile = request.body.files.head.ref


    val uuid = UUID.randomUUID().toString
    // when the upload is complete, we add the article id to the file entry (in order to find the attachments of the article)
    val futureUpdate = for {
                                       file <- futureFile

      updateResult <- {
        gridFS.files.update(
          BSONDocument("_id" -> file.id),
          BSONDocument("$set" ->
              BSONDocument("metadata" -> BSONDocument("UUID" -> uuid, "size" -> "normal"))
          )
        )


        val iterator = gridFS.enumerate[JSONSerializationPack.Value](file).run(Iteratee.consume[Array[Byte]]())
        iterator.flatMap {
          bytes => {
            // Create resized image
            val enumerator: Enumerator[Array[Byte]] = Enumerator.outputStream(
              out => {
                Image(bytes).bound(120, 120).writer(Format.JPEG).withCompression(90).write(out)
              }
            )

            val data = JSONFileToSave(
              filename = file.filename,
              contentType = file.contentType,
              uploadDate = Some(DateTime.now().getMillis),
              metadata =  Json.obj(
                "UUID" -> uuid,
                "size" -> "thumb"
              )
            )
            //gridFS.save(enumerator, data.asInstanceOf[FileToSave[gridFS.pack.type, gridFS.pack.Value]]).map {
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


  type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]

  def getAttachment(uuid: String, size: String) = Action.async { request =>
    // find the matching attachment, if any, and streams it to the client
    val file = gridFS.find[JsObject, JSONReadFile](Json.obj("metadata.UUID" -> uuid, "metadata.size" -> size ))

    request.getQueryString("inline") match {
      case Some("true") =>
        serve[JsString, JSONReadFile](gridFS)(file, CONTENT_DISPOSITION_INLINE)

      case _            => serve[JsString, JSONReadFile](gridFS)(file)
    }
  }
}
