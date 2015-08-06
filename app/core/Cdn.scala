package core

import scala.util.Random

object Cdn {
  var maybeAssetsUrl = Option("")
  var maybeAssetsUrl2 = Option("")

  def assetCDN(url: String): String = {
    val result =
      if(url.take(2) == "//")
        url
      else {
        val rand = Random.nextBoolean()
        if(rand)
          Cdn.maybeAssetsUrl.fold(url)(_ + url)
        else
          Cdn.maybeAssetsUrl2.fold(url)(_ + url)
      }
    result
  }
}