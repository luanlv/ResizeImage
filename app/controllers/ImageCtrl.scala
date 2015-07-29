package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Request}

import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import MongoController.readFileReads

import play.modules.reactivemongo.json.JSONSerializationPack
import reactivemongo.api.QueryOpts
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONDocument
import models.Image
import models.Image._
import reactivemongo.api.gridfs.Implicits._
import core.dao.ImageDAO
import java.util.UUID
import play.modules.reactivemongo.json._
import reactivemongo.bson._
import play.api.libs.json._

import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

class ImageCtrl @Inject() (
    val messagesApi: MessagesApi,
    val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents {

  type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]


  val cImage = db[JSONCollection]("fs.files")

  val gridFS = reactiveMongoApi.gridFS

  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  def getList(name: String, page: Int) = Action.async { request =>


    val futureJson = cImage.find(Json.obj(
            "metadata.size" -> "small",
            "filename" -> Json.obj("$regex" ->  (".*" + name + ".*"), "$options" -> "-i")))

    val futureTotalPage = futureJson.cursor().collect[List]().map(x => Math.ceil(x.length/12.0).toInt)
    val futureList = futureJson.sort(Json.obj("uploadDate" -> -1))
        .options(QueryOpts((page-1) * 12))
//        .options(QueryOpts(0))
        .cursor[Image]()
        .collect[List](12)

    val futureView = for {
      total <- futureTotalPage
      list <- futureList
    } yield (total, list)

    futureView.map{
      data => Ok(views.html.image.list_Image(data._2, data._1, page, name))
    }
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
              BSONDocument("metadata" -> BSONDocument("uuid" -> uuid, "size" -> "origin"))
          )
        )
        // Create resized image
        ImageDAO.scaleTo(gridFS, image, uuid, "small", 180, 150)
        ImageDAO.scaleTo(gridFS, image, uuid, "thumb", 60, 60)
      }
    } yield (updateResult, image.id, image.filename, image.length)

    futureUpdate.map {
      case (_, id,  filename, length) => {
        Ok(Json.obj("files" -> Seq(Json.obj(
          "id" -> id,
          "name" -> filename,
          "size" -> length,
          "url" -> routes.ImageCtrl.get(uuid, "origin").url,
          "thumbnailUrl" -> routes.ImageCtrl.get(uuid, "thumb").url,
          "deleteUrl" -> "",
          "deleteType" -> "DELETE"
        ))))
      }
    }.recover {
      case e => {
        println(e.getMessage())
        InternalServerError(e.getMessage())
        Ok("error")
      }
    }
  }

  def get(uuid: String, size: String) = Action.async { request =>
    val image = ImageDAO.get(gridFS, uuid, size)
    serve[JsString, JSONReadFile](gridFS)(image, CONTENT_DISPOSITION_INLINE).map(_.withHeaders("Cache-Control" ->"max-age=%d, public".format(60*60*24*365)))
  }



}
