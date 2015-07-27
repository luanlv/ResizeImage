package models

import com.sun.corba.se.spi.ior.ObjectId
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._

import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONObjectID}
import play.modules.reactivemongo.json.BSONFormats._


case class Metadata(uuid: String, size: String)

object Metadata {


  implicit object MetadataWrites extends OWrites[Metadata] {
    def writes(metadata: Metadata): JsObject = Json.obj(
      "uuid" -> metadata.uuid,
      "size" -> metadata.size
    )
  }

  implicit object MetadataReads extends Reads[Metadata] {
    def reads(json: JsValue): JsResult[Metadata] = json match {
      case obj: JsObject => try {
        val uuid = (obj \ "uuid").as[String]
        val size = (obj \ "size").as[String]
        JsSuccess(Metadata(uuid, size))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }
}

case class Image(filename: String, length: Int, contentType: String, metadata: Metadata)

object Image {

  implicit object ImageWrites extends OWrites[Image] {
    def writes(image: Image): JsObject = Json.obj(
      "filename" -> image.filename,
      "length" -> image.length,
      "contentType" -> image.contentType,
      "metadata" -> image.metadata
    )
  }

  implicit object ImageReads extends Reads[Image] {
    def reads(json: JsValue): JsResult[Image] = json match {
      case obj: JsObject => try {
        val filename = (obj \ "filename").as[String]
        val length = (obj \ "length").as[Int]
        val contentType = (obj \ "contentType").as[String]
        val metadata = (obj \ "metadata").as[Metadata]
        JsSuccess(Image(filename, length, contentType, metadata))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }
}
