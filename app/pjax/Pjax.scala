package pjax

import globals.Global
import play.api.mvc._
import scala.concurrent.Future

trait Pjax {

  self: Controller =>

  implicit val pjaxVersion = PjaxVersion(Global.pjaxVersion)

  class PjaxRequest[A](val pjaxEnabled: Boolean, request: Request[A]) extends WrappedRequest[A](request)

  object PjaxAction extends ActionBuilder[PjaxRequest] with ActionTransformer[Request, PjaxRequest] {

    private val pjaxHeader = "X-PJAX"

    def transform[A](request: Request[A]) = Future.successful {
      new PjaxRequest(request.headers.get(pjaxHeader).contains("true"), request)
    }
  }

}

case class PjaxVersion(value: String) extends AnyVal

object PjaxVersionFilter extends Filter {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val pjaxVersionHeader = "X-PJAX-Version"

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).map { result => result.withHeaders(pjaxVersionHeader -> Global.pjaxVersion) }
  }

}



