package actors

import scala.collection.immutable.SortedSet
import akka.actor._
import akka.persistence._
import akka.event.Logging
import hex._

import scala.util.Random

case class AddErrorMessage(weight: Double, text: String)
case class ErrorMessage(weight: Double, text: String)
case object GetState

class ErrorMessageStore extends PersistentActor with RandomResponse[String] {
  type State = SortedSet[(Double, String)]

  override val persistenceId = "error-message-component"

  val log = Logging(context.system, this)

  var state: State = SortedSet()

  def updated(e: ErrorMessage) = 
    state = state + (e.weight -> e.text)

  val receiveRecover: Receive = {
    case e: ErrorMessage => updated(e)
    case SnapshotOffer(_, snap: State) ⇒ state = snap
  }

  val receiveCommand: Receive = {
    case AddErrorMessage(weight, text) ⇒ 
      persist(ErrorMessage(weight, text)) { event =>
        log.debug(s"ErrorMessageStore add message: ${event}")
        updated(event)
        log.debug(s"ErrorMessageStore current state: ${state}")
        self ! "snap"
        context.system.eventStream.publish(event)
      }
    case "snap"  => saveSnapshot(state)
    case "print" => println(state)
    case q: Question ⇒ 
      log.debug(s"ErrorMessageStore received: ${q} at state ${state}")
      val res = response(state).map(Response).getOrElse(Failed)
      log.debug(s"ErrorMessageStore sends: ${res}")
      sender() ! res
  }
}

object ErrorMessageStore {
  def props = Props(new ErrorMessageStore)
}

class ErrorMessageView(coordinator: ActorRef) extends PersistentView with RandomResponse[String] {
  type State = SortedSet[(Double, String)]

  val log = Logging(context.system, this)

  override val persistenceId = "error-message-component"
  override val viewId = "error-message-view-" + java.util.UUID.randomUUID.toString

  var state: State = SortedSet()

  def receive: Receive = {
    case payload: ErrorMessage if isPersistent => state = state + (payload.weight -> payload.text)
    case SnapshotOffer(metadata, savedState: State) => state = savedState
    case q: Question ⇒ 
      log.debug(s"ErrorMessageView received: ${q} at state ${state}")
      val res = response(state).map(Response).getOrElse(Failed)
      log.debug(s"ErrorMessageView sends: ${res}")
      sender() ! res
  }
}

object ErrorMessageView {
  def props(out: ActorRef) = Props(new ErrorMessageView(out))
}
