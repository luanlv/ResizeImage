import play.api.mvc.WithFilters
import play.api.{GlobalSettings, Play}
import play.api.Play.current
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.mohiva.play.htmlcompressor._
import play.filters.gzip.GzipFilter

/**
 * Uses a user-defined implementation of the HTML compressor filter.
 */
object Global extends WithFilters(new GzipFilter(), HTMLCompressorFilter()) with GlobalSettings {

}

/**
 * Defines a user-defined HTML compressor filter.
 */
object HTMLCompressorFilter {
  /**
   * Creates the HTML compressor filter.
   *
   * @return The HTML compressor filter.
   */
  def apply() = new HTMLCompressorFilter({
    val compressor = new HtmlCompressor()
    if (Play.isDev) {
      compressor.setPreserveLineBreaks(true)
    }
    compressor.setRemoveComments(true)
    compressor.setRemoveIntertagSpaces(true)
    compressor.setRemoveHttpProtocol(true)
    compressor.setRemoveHttpsProtocol(true)
    compressor.setRemoveComments(true)
    compressor.setRemoveJavaScriptProtocol(true)
    compressor.setRemoveStyleAttributes(true)
    compressor
  })
}