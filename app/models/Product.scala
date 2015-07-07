package models


import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, optional, text}
import play.api.data._
import play.api.data.validation.Constraints.pattern


case class Product(
                      id: Option[String],
                      image: Option[String],
                      code: String,
                      name: String,
                      unit: String,
                      stock: Int,
                      price: Int,
                      brand: Option[String],
                      group: Option[String],
                      origin: Option[String],
                      legType: Option[String],
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
      "image" -> product.image,
      "code" -> product.code,
      "name" -> product.name,
      "unit" -> product.unit,
      "stock" -> product.stock,
      "price" -> product.price,
      "brand" -> product.brand,
      "group" -> product.group,
      "origin" -> product.origin,
      "legType" -> product.legType,
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
        val image = (obj \ "image").asOpt[String]
        val code = (obj \ "code").as[String]
        val name = (obj \ "name").as[String]
        val unit = (obj \ "unit").as[String]
        val stock = (obj \ "stock").as[Int]
        val price = (obj \ "price").as[Int]
        val brand = (obj \ "brand").asOpt[String]
        val group = (obj \ "group").asOpt[String]
        val origin = (obj \ "origin").asOpt[String]
        val legType = (obj \ "legType").asOpt[String]
        val info = (obj \ "info").asOpt[String]
        val note = (obj \ "note").asOpt[String]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        val updateDate = (obj \ "updateDate").asOpt[Long]
        JsSuccess(Product(id, image, code, name, unit, stock, price, brand, group, origin, legType,
            info, note, creationDate.map(new DateTime(_)), updateDate.map(new DateTime(_))))
      } catch {
        case cause: Throwable  => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }


  val form = Form(
    mapping(
      "id" -> optional(text verifying pattern(
        """[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      "image" -> optional(text),
      "code" -> nonEmptyText,
      "name" -> nonEmptyText,
      "unit" -> nonEmptyText,
      "stock" -> number,
      "price" -> number,
      "brand" -> optional(text),
      "group" -> optional(text),
      "origin" -> optional(text),
      "legType" -> optional(text),
      "info" -> optional(text),
      "note" -> optional(text),
      "creationDate" -> optional(longNumber),
      "updateDate" -> optional(longNumber)
    ){
      (id, image, code, name, unit, stock, price, brand, group, origin, legType, info, note,
          creationDate, updateDate) =>
        Product(
          id,
          image,
          code,
          name,
          unit,
          stock,
          price,
          brand,
          group,
          origin,
          legType,
          info,
          note,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))
        )
    } {
      product =>
        Some(
          product.id,
          product.image,
          product.code,
          product.name,
          product.unit,
          product.stock,
          product.price,
          product.brand,
          product.group,
          product.origin,
          product.legType,
          product.info,
          product.note,
          product.creationDate.map(_.getMillis),
          product.updateDate.map(_.getMillis)
        )
    }
  )
}
