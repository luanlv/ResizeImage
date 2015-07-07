package controllers

import play.api.mvc.{Action, Controller, Request}


class Application  extends Controller{

  def index = Action { implicit request =>
    Ok("ok")
  }
  // list all articles and sort them
  def upload = Action { implicit request =>
     Ok(views.html.image.upload())
  }

  def test = Action {
    Ok(views.html.test())
  }
}
