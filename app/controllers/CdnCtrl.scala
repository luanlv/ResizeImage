package controllers

import core.Cdn._
import play.api.mvc._

class CdnCtrl extends Controller {


  def clearCDN() = Action {
    maybeAssetsUrl = Option("")
    maybeAssetsUrl2 = Option("")
    Ok("clear CDN ok!")
  }

  def setCDN1(name: String) = Action {
    val newCdn = "//" + name
    maybeAssetsUrl = Option(newCdn)
    Ok("Set cdn1 to: " + name + " ok!")
  }

  def setCDN2(name: String) = Action {
    val newCdn = "//" + name
    maybeAssetsUrl2 = Option(newCdn)
    Ok("Set cdn2 to: " + name + " ok!")
  }

  def setCDN3(name: String) = Action {
    val newCdn = "//" + name
    maybeAssetsUrl3 = Option(newCdn)
    Ok("Set cdn2 to: " + name + " ok!")
  }

}
