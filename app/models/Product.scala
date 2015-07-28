package models


import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data._
import play.api.data.validation.Constraints.pattern
import play.api.libs.json._

case class ImageUrl(origin: String, small: String, thumb: String)



case class ListPrice(num: Int, price: Int)


case class Url(supType: String, subType: String, group: String, pUrl: String, tag: Option[List[String]])

case class Core(code: String, name: String, price: List[ListPrice])


case class Info(supType: String, subType: String, group: String, image: List[ImageUrl], unit: String, stock: Int, sold: Int, vote: Int,  brand:String,
                origin: String, legType: String, legNumber: String)

case class Extra(saleOff1: Int, saleOff2: Int, info: String, note: String)

case class Product(id: Option[String], url: Url, core: Core, info: Info, extra: Extra,
                      creationDate: Option[DateTime], updateDate: Option[DateTime])

object ImageUrl{

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

object ListPrice{
  implicit object ListPriceWrite extends OWrites[ListPrice]{
    def writes(mutilPrice: ListPrice): JsObject = Json.obj(
      "num" -> mutilPrice.num,
      "price" -> mutilPrice.price
    )
  }

  implicit object ListPriceRead extends Reads[ListPrice]{
    def reads(json: JsValue): JsResult[ListPrice] = json match {
      case obj: JsObject => try {
        val num = (obj \ "num").as[Int]
        val price = (obj \ "price").as[Int]
        JsSuccess(ListPrice(num, price))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }
}

object Url{
  implicit object UrlWrite extends OWrites[Url]{
    def writes(url: Url): JsObject = Json.obj(
      "supType" -> url.supType,
      "subType" -> url.supType,
      "group" -> url.group,
      "pUrl" -> url.pUrl,
      "tag" -> url.tag
    )
  }

  implicit object UrlRead extends Reads[Url] {
    def reads(json: JsValue): JsResult[Url] = json match {
      case obj: JsObject => try {
        val supType = (obj \ "supType").as[String]
        val subType = (obj \ "subType").as[String]
        val group = (obj \ "group").as[String]
        val pUrl = (obj \ "pUrl").as[String]
        val tag = (obj \ "tag").asOpt[List[String]]
        JsSuccess(Url(supType, subType, group, pUrl, tag))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.JsObject")
    }
  }
}

object Core {
  implicit object CoreWrite extends OWrites[Core]{
    def writes(obj: Core): JsObject = Json.obj(
      "code" -> obj.code,
      "name" -> obj.name,
      "price" -> Json.toJsFieldJsValueWrapper(obj.price)
    )
  }
  implicit object CoreRead extends Reads[Core] {
    def reads(json: JsValue): JsResult[Core] = json match {
      case obj: JsObject => try {
        val code = (obj \ "code").as[String]
        val name = (obj \ "name").as[String]
        val price = (obj \ "price").as[List[ListPrice]]
        JsSuccess(Core(code, name, price))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.JsObject")
    }
  }
}


object Info {
  implicit object InfoWrite extends OWrites[Info]{
    def writes(obj: Info): JsObject = Json.obj(
      "supType" -> obj.supType,
      "subType" -> obj.subType,
      "group" -> obj.group,
      "image" -> Json.toJsFieldJsValueWrapper(obj.image),
      "unit" -> obj.unit,
      "stock" -> obj.stock,
      "sold" -> obj.sold,
      "vote" -> obj.vote,
      "brand" -> obj.brand,
      "origin" -> obj.origin,
      "legType" -> obj.legType,
      "legNumber" -> obj.legNumber
    )
  }
  implicit object InfoRead extends Reads[Info] {
    def reads(json: JsValue): JsResult[Info] = json match {
      case obj: JsObject => try {
        val supType = (obj \ "supType").as[String]
        val subType = (obj \ "subType").as[String]
        val group = (obj \ "group").as[String]
        val image = (obj \ "image").as[List[ImageUrl]]
        val unit = (obj \ "unit").as[String]
        val stock = (obj \ "stock").as[Int]
        val sold = (obj \ "sold").as[Int]
        val vote = (obj \ "vote").as[Int]
        val brand = (obj \ "brand").as[String]
        val origin = (obj \ "legType").as[String]
        val legType = (obj \ "legType").as[String]
        val legNumber = (obj \ "legNumber").as[String]
        JsSuccess(Info(supType, subType, group, image, unit, stock, sold, vote, brand, origin, legType, legNumber))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.JsObject")
    }
  }
}

object Extra {
  implicit object ExtraWrite extends OWrites[Extra]{
    def writes(obj: Extra): JsObject = Json.obj(
      "saleOff1" -> obj.saleOff1,
      "saleoff2" -> obj.saleOff2,
      "info" -> obj.info,
      "note" -> obj.note
    )
  }
  implicit object ExtraRead extends Reads[Extra] {
    def reads(json: JsValue): JsResult[Extra] = json match {
      case obj: JsObject => try {
        val saleOff1 = (obj \ "saleOff1").as[Int]
        val saleoff2 = (obj \ "saleOff2").as[Int]
        val info = (obj \ "info").as[String]
        val note = (obj \ "note").as[String]
        JsSuccess(Extra(saleOff1, saleoff2, info, note))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.JsObject")
    }
  }
}




object Product{
  import play.api.libs.json._
  implicit object ProductWrites extends OWrites[Product] {
    def writes(product: Product): JsObject = Json.obj(
      "_id" -> product.id,
      "url" -> Json.toJsFieldJsValueWrapper(product.url),
      "core" -> Json.toJsFieldJsValueWrapper(product.core),
      "info" -> Json.toJsFieldJsValueWrapper(product.info),
      "extra" -> Json.toJsFieldJsValueWrapper(product.extra),
      "creationDate" -> product.creationDate.fold(-1L)(_.getMillis),
      "updateDate" -> product.updateDate.fold(-1L)(_.getMillis)
    )
  }

  implicit object ProductReads extends Reads[Product] {
    def reads(json: JsValue): JsResult[Product] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").asOpt[String]
        val url = (obj \ "url").as[Url]
        val core = (obj \ "core").as[Core]
        val info = (obj \ "info").as[Info]
        val extra = (obj \ "extra").as[Extra]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        val updateDate = (obj \ "updateDate").asOpt[Long]
        JsSuccess(Product(id, url, core, info, extra,
          creationDate.map(new DateTime(_)), updateDate.map(new DateTime(_))))
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
      "url" -> mapping(
        "supType" -> nonEmptyText,
        "subType" -> nonEmptyText,
        "group" -> nonEmptyText,
        "pUrl" ->nonEmptyText,
        "tag" -> optional(list(text))
        )(Url.apply)(Url.unapply),

      "core" -> mapping(
        "code" -> nonEmptyText,
        "name" -> nonEmptyText,
        "price" -> list(mapping(
          "num" -> number.verifying(min(0)),
          "price" -> number.verifying(min(0))
          )(ListPrice.apply)(ListPrice.unapply))
        )(Core.apply)(Core.unapply),
      "info" -> mapping(
        "supType" -> text,
        "subType" -> text,
        "group" -> text,
        "image" -> list(mapping(
            "origin" -> text,
            "small" -> text,
            "thumb" -> text
            )(ImageUrl.apply)(ImageUrl.unapply)
            ),
        "unit" -> text,
        "stock" -> number.verifying(min(0)),
        "sold" -> number.verifying(min(0)),
        "vote" -> number,
        "brand" -> text,
        "origin" -> text,
        "legType" -> text,
        "legNumber" -> text
        )(Info.apply)(Info.unapply),
      "extra" -> mapping(
        "saleOff1" -> number.verifying(min(0)),
        "saleOff1" -> number.verifying(min(0)),
        "info" -> text,
        "note" -> text
        )(Extra.apply)(Extra.unapply),
      "creationDate" -> optional(longNumber),
      "updateDate" -> optional(longNumber)
    ){
      (id, url, core, info, extra, creationDate, updateDate) =>
        Product(
          id,
          url,
          core,
          info,
          extra,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))
        )
    } {
      product =>
        Some(
          product.id,
          product.url,
          product.core,
          product.info,
          product.extra,
          product.creationDate.map(_.getMillis),
          product.updateDate.map(_.getMillis)
        )
    }
  )
}
