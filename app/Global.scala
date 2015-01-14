import play.api._
import play.api.mvc._
import play.filters.gzip.GzipFilter

import actors._

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {
	import play.api.Play.current
	import play.api.libs.concurrent.Akka

	override def onStart(app: Application) {
    val errorStore = Akka.system.actorOf(ErrorMessageStore.props, "error-message-store")
  }
}