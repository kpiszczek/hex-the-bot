package actors

import akka.actor._
import akka.event.Logging
import lambda.LambdaReductor

class LambdaActor(coordinator: ActorRef) extends Actor with LambdaReductor {

	val log = Logging(context.system, this)

	def receive: Receive = {
		case Question(str) => 
			log.debug(s"LambdaActor received: ${str}")
			sender() ! reduce(str).map(Response).getOrElse(Failed)
	}
}

object LambdaActor {
	def props(out: ActorRef) = Props(new LambdaActor(out))
}