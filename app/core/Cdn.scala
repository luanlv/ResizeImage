package core

import scala.util.Random

object Cdn {
  var maybeAssetsUrl = Option("")

  def assetCDN(url: String): String = {
    val result =
      if(url.take(2) == "//")
        url
      else
        Cdn.maybeAssetsUrl.fold(url)(_ + url)
    result
  }
}