import javax.inject.Inject

import play.api.{GlobalSettings, Application}
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import controllers.CdnCtrl

class Filters @Inject() (gzipFilter: GzipFilter) extends HttpFilters {
  def filters = Seq(gzipFilter)
}

object Global extends GlobalSettings {

  override def onStart(app: Application) {

  }

}

object InitialData {
  def setupConfig() = {

  }
}