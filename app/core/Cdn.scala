package core

import scala.util.Random

object Cdn {
  var maybeAssetsUrl = Option("")
  var maybeAssetsUrl2 = Option("")
  var maybeAssetsUrl3 = Option("")
  var maybeAssetsUrl4 = Option("")

  def assetCDN(url: String): String = {
    val result =
      if(url.take(2) == "//")
        url
      else {
        val rand = Random.nextDouble()
        if(rand<0.25)
          Cdn.maybeAssetsUrl.fold(url)(_ + url)
        else
          if(rand<0.5)
            Cdn.maybeAssetsUrl2.fold(url)(_ + url)
          else
            if(rand<0.75)
              Cdn.maybeAssetsUrl3.fold(url)(_ + url)
            else
              Cdn.maybeAssetsUrl4.fold(url)(_ + url)
      }
    result
  }
}