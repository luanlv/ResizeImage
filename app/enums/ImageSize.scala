package enums


object ImageSize extends Enumeration {

  case class ImageSizeValue(name: String, max: Int) extends Val(name)

  val Original = ImageSizeValue("default", 0) // Original dimensions, no resizing
  val Large = ImageSizeValue("large", 960)
  val Medium = ImageSizeValue("medium", 480)
  val Small = ImageSizeValue("small", 240)
  val Icon = ImageSizeValue("thumb", 120)

  implicit def valueToSize(v: Value): ImageSizeValue = v.asInstanceOf[ImageSizeValue]
}
