package controllers


import javax.inject.Inject

import play.api.Configuration
import play.api.mvc.{Action, Controller, Request}


class Application @Inject() (config: Configuration)  extends Controller{

  def upload = Action { implicit request =>
    Ok(views.html.image.upload())
  }

  def assetUrl(file: String): String = {
    val versionedUrl = routes.Assets.versioned(file).url
    val maybeAssetsUrl = config.getString("assets.url")
    maybeAssetsUrl.fold(versionedUrl)(_ + versionedUrl)
  }

}
