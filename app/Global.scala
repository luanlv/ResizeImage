import com.google.inject.{Guice, AbstractModule}
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

/**
 * Set up the Guice injector and provide the mechanism for return objects from the dependency graph.
 */
object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

}
