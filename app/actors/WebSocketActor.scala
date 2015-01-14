package actors

import play.api.libs.json._
import akka.actor._
import akka.event.Logging

sealed trait Message
case class Question(get: String) extends Message
case class Response(get: String) extends Message
case object Failed extends Message

class WebSocketActor(channel: ActorRef) extends Actor {
	val log = Logging(context.system, this)
	val serializer = context.actorOf(SerializerActor.props(channel))
	val coordinator = context.actorOf(Coordinator.props(serializer))

	def deserializeMsg(msg: JsValue): Option[Message] = 
		(msg \ "kind").as[String] match {
			case "question" => Option(Question((msg \ "message").as[String]))
			case _ => None
		} 

	def receive: Receive = {
		case msg: JsValue => 
			log.debug(s"WebSocketActor received: ${msg}")
			deserializeMsg(msg).foreach(m => coordinator ! m)
	}
}

object WebSocketActor {
	def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class SerializerActor(out: ActorRef) extends Actor {
	val log = Logging(context.system, this)
	def receive: Receive = {
		case Response(msg) => 
			log.debug(s"SerializerActor received: ${msg}")
			out ! Json.obj(
				"kind" -> "response",
				"message" -> msg
			)
		case Failed => 
			log.debug(s"SerializerActor received: Failed")
			out ! Json.obj(
				"kind" -> "error",
				"message" -> "error"
			)
	}
}

object SerializerActor {
	def props(out: ActorRef) = Props(new SerializerActor(out))
}