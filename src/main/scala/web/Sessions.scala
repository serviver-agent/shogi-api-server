package web

import java.util.UUID
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

final case class SessionId(value: UUID) {
  def asString: String = value.toString
}
object SessionId {
  def create(): SessionId               = SessionId(UUID.randomUUID())
  def fromString(id: String): SessionId = SessionId(UUID.fromString(id))
}

final case class SessionInfo(sessionId: SessionId)

object Sessions {

  sealed trait Command
  final case class CreateSession(replyTo: ActorRef[ActorRef[Session.Command]])                     extends Command
  final case class GetSession(id: SessionId, replyTo: ActorRef[Option[ActorRef[Session.Command]]]) extends Command
  final case class DeleteSession(id: SessionId)                                                    extends Command

  def apply[T](): Behavior[Command] = sessions(Map.empty)

  def sessions[T](sessionMap: Map[SessionId, ActorRef[Session.Command]]): Behavior[Command] = Behaviors.receive {
    (context, command) =>
      command match {
        case CreateSession(replyTo) =>
          val id                             = SessionId.create()
          val ref: ActorRef[Session.Command] = context.spawnAnonymous(Session(id))
          replyTo ! ref
          sessions(sessionMap + (id -> ref))
        case GetSession(id, replyTo) =>
          replyTo ! sessionMap.get(id)
          Behaviors.same
        case DeleteSession(id) =>
          sessionMap.get(id).foreach(ref => context.stop(ref))
          sessions(sessionMap - id)
      }
  }

}

object Session {

  sealed trait Command
  final case class GetInfo(replyTo: ActorRef[SessionInfo]) extends Command

  def apply(sessionId: SessionId): Behavior[Command] = session(SessionInfo(sessionId))

  def session(info: SessionInfo): Behavior[Command] = Behaviors.receiveMessage {
    case GetInfo(replyTo) =>
      replyTo ! info
      Behaviors.same
  }

}
