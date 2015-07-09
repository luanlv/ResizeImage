import play.api.mvc.WithFilters
import play.api.{GlobalSettings, Play}

import play.filters.gzip.GzipFilter

/**
 * Uses a user-defined implementation of the HTML compressor filter.
 */
object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

}