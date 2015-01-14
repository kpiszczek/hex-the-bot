package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import actors.WebSocketActor

object Application extends Controller {
	import play.api.Play.current
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def chat = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
  	WebSocketActor.props(out)
	}

	def chatRoom = Action { implicit request =>
    Ok(views.html.chat(request))
  }
}