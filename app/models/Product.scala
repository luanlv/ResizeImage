package models


import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data._
import play.api.data.validation.Constraints.pattern
import play.api.libs.json._

case class ImageUrl(origin: String, small: String, thumb: String)

object ImageUrl{
  import play.api.libs.json._
  implicit object ImageUrlWrites extends OWrites[ImageUrl] {
    def writes(imageUrl: ImageUrl): JsObject = Json.obj(
      "origin" -> imageUrl.origin,
      "small" -> imageUrl.small,
      "thumb" -> imageUrl.thumb
    )
  }

  implicit object ImageUrlReads extends Reads[ImageUrl] {
    def reads(json: JsValue): JsResult[ImageUrl] = json match {
      case obj: JsObject => try {
        val origin = (obj \ "origin").as[String]
        val small = (obj \ "small").as[String]
        val thumb = (obj \ "thumb").as[String]

        JsSuccess(ImageUrl(origin, small, thumb))
      } catch {
        case cause: Throwable  => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

}

case class Product(
                      id: Option[String],
                      supTypeUrl: String,
                      supType: String,
                      subTypeUrl: String,
                      subType: String,
                      pUrl: String,
                      image: List[ImageUrl],
                      code: String,
                      name: String,
                      unit: String,
                      stock: Int,
                      price: Int,
                      groupUrl: String,
                      group: String,
                      brand: String,
                      origin: String,
                      legType: String,
                      legNumber: String,
                      info: Option[String],
                      note: Option[String],
                      creationDate: Option[DateTime],
                      updateDate: Option[DateTime]
                      )

object Product{
  import play.api.libs.json._
  implicit object ProductWrites extends OWrites[Product] {
    def writes(product: Product): JsObject = Json.obj(
      "_id" -> product.id,
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
      "creationDate" -> product.creationDate.fold(-1L)(_.getMillis),
      "updateDate" -> product.updateDate.fold(-1L)(_.getMillis)
    )
  }

  implicit object ProductReads extends Reads[Product] {
    def reads(json: JsValue): JsResult[Product] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").asOpt[String]
        val supTypeUrl = (obj \ "supTypeUrl").as[String]
        val supType = (obj \ "supType").as[String]
        val subTypeUrl = (obj \ "subTypeUrl").as[String]
        val subType = (obj \ "subType").as[String]
        val pUrl = (obj \ "pUrl").as[String]
        val image = (obj \ "image").as[List[ImageUrl]]
        val code = (obj \ "code").as[String]
        val name = (obj \ "name").as[String]
        val unit = (obj \ "unit").as[String]
        val stock = (obj \ "stock").as[Int]
        val price = (obj \ "price").as[Int]
        val groupUrl = (obj \ "groupUrl").as[String]
        val group = (obj \ "group").as[String]
        val brand = (obj \ "brand").as[String]
        val origin = (obj \ "origin").as[String]
        val legType = (obj \ "legType").as[String]
        val legNumber = (obj \ "legNumber").as[String]
        val info = (obj \ "info").asOpt[String]
        val note = (obj \ "note").asOpt[String]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        val updateDate = (obj \ "updateDate").asOpt[Long]
        JsSuccess(Product(id, supTypeUrl, supType, subTypeUrl, subType, pUrl, image, code, name, unit, stock, price, groupUrl, group, brand, origin, legType, legNumber,
            info, note, creationDate.map(new DateTime(_)), updateDate.map(new DateTime(_))))
      } catch {
        case cause: Throwable  => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }


  val form: Form[Product] = Form(
    mapping(
      "id" -> optional(text verifying pattern(
        """[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      "supTypeUrl" -> nonEmptyText,
      "supType" -> nonEmptyText,
      "subTypeUrl" -> nonEmptyText,
      "subType" -> nonEmptyText,
      "pUrl" -> nonEmptyText,
      "image" -> list(mapping("origin" -> text, "small" -> text, "thumb" -> text)(ImageUrl.apply)(ImageUrl.unapply)),
      "code" -> nonEmptyText,
      "name" -> nonEmptyText,
      "unit" -> nonEmptyText,
      "stock" -> number,
      "price" -> number,
      "groupUrl" -> nonEmptyText,
      "group" -> nonEmptyText,
      "brand" -> text,
      "origin" -> text,
      "legType" -> text,
      "legNumber" -> text,
      "info" -> optional(text),
      "note" -> optional(text),
      "creationDate" -> optional(longNumber),
      "updateDate" -> optional(longNumber)
    ){
      (id, supTypeUrl, supType, subTypeUrl, subType, pUrl, image, code, name, unit, stock, price, groupUrl, group, brand, origin, legType, legNumber, info, note,
          creationDate, updateDate) =>
        Product(
          id,
          supTypeUrl,
          supType,
          subTypeUrl,
          subType,
          pUrl,
          image,
          code,
          name,
          unit,
          stock,
          price,
          groupUrl,
          group,
          brand,
          origin,
          legType,
          legNumber,
          info,
          note,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))
        )
    } {
      product =>
        Some(
          product.id,
          product.supTypeUrl,
          product.supType,
          product.subTypeUrl,
          product.subType,
          product.pUrl,
          product.image,
          product.code,
          product.name,
          product.unit,
          product.stock,
          product.price,
          product.groupUrl,
          product.group,
          product.brand,
          product.origin,
          product.legType,
          product.legNumber,
          product.info,
          product.note,
          product.creationDate.map(_.getMillis),
          product.updateDate.map(_.getMillis)
        )
    }
  )
}
