package globals

import pjax.PjaxVersionFilter
import play.api.mvc.WithFilters

object Global extends WithFilters(PjaxVersionFilter) {
  val pjaxVersion = "v1"
}



