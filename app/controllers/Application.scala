package controllers

import play.api.mvc.{Action, Controller, Request}


class Application  extends Controller{

  def index = Action { implicit request =>
    Redirect(routes.Application.upload())
  }
  // list all articles and sort them
  def upload = Action { implicit request =>
     Ok(views.html.upload())
  }

}
