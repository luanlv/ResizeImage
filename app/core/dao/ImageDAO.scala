package core.dao

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import com.sksamuel.scrimage.nio.JpegWriter
import org.joda.time.DateTime
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.modules.reactivemongo.{MongoController, JSONFileToSave}
import play.modules.reactivemongo.json.{ImplicitBSONHandlers, JSONSerializationPack}
import reactivemongo.api.gridfs.{ReadFile, GridFS}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import ImplicitBSONHandlers._
import MongoController.readFileReads
import play.modules.reactivemongo.json._

object ImageDAO {
  type G = GridFS[JSONSerializationPack.type]

  def get(gfs: G, uuid: String, size: String) = {
    type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]
    gfs.find[JsObject, JSONReadFile](Json.obj("metadata.uuid" -> uuid, "metadata.size" -> size ))
  }

  def scaleTo(gfs: G, f:  ReadFile[JSONSerializationPack.type, JsValue], uuid: String, width: Int, height: Int) = {
    val iterator = gfs
        .enumerate[JSONSerializationPack.Value](f)
        .run(Iteratee.consume[Array[Byte]]())

    iterator.flatMap {
      bytes => {
        val enumerator: Enumerator[Array[Byte]] = Enumerator.outputStream(
          out => {
            implicit val writer = JpegWriter().withProgressive(true)
            Image(bytes).scaleTo(width, height, Bicubic).forWriter(writer).write(out)
          }
        )

        val data = JSONFileToSave(
          filename = f.filename,
          contentType = f.contentType,
          uploadDate = Some(DateTime.now().getMillis),
          metadata =  Json.obj(
            "uuid" -> uuid,
            "size" -> "thumb"
          )
        )
        gfs.save(enumerator, data).map {
          image => Some(image)
        }
      }
    }
  }
}
