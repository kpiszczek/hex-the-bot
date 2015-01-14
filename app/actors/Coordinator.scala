package actors

import akka.actor._
import scala.concurrent.Future
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.event.Logging

class Coordinator(channel: ActorRef) extends Actor {
	import scala.concurrent.ExecutionContext.Implicits.global
	implicit val timeout = Timeout(5.seconds) 
	val log = Logging(context.system, this)
	val errorActor = context.actorOf(ErrorMessageView.props(self))
	val lambdaActor = context.actorOf(LambdaActor.props(self))
  def receive: Receive = {
  	case q: Question => 
  		log.debug(s"Coordinator received: ${q}")
  		send(q)
  }

  def send(q: Question): Unit = {
  	val fLambda = lambdaActor ? q
  	val fError = errorActor ? q
  	fLambda flatMap {
  		case r: Response => Future.successful(r)
  		case Failed => fError
  	} pipeTo channel
  }
}

object Coordinator {
	def props(out: ActorRef) = Props(new Coordinator(out))
}