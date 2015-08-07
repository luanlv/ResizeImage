package controllers

import core.Cdn._
import play.api.mvc._

class CdnCtrl extends Controller {


  def clearCDN() = Action {
    maybeAssetsUrl = Option("")
    Ok("clear CDN ok!")
  }

  def setCDN(name: String) = Action {
    val newCdn = "//" + name
    maybeAssetsUrl = Option(newCdn)
    Ok("Set cdn1 to: " + name + " ok!")
  }

}
