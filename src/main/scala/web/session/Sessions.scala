package web.session

import java.util.UUID
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed

object Sessions {

  sealed trait Command
  object Command {
    case class Get(bearer: Bearer, replyTo: ActorRef[Option[SessionInfo]]) extends Command
    case class Create(replyTo: ActorRef[SessionInfo])                      extends Command
    case class Delete(bearer: Bearer)                                      extends Command
  }

  case class SessionInfo(bearer: Bearer, sessionId: SessionId)

  def apply(): Behavior[Command] = sessions(Map.empty)

  def sessions(
      map: Map[Bearer, SessionInfo]
  ): Behavior[Command] = Behaviors.receiveMessage {
    case Command.Get(bearer, replyTo) =>
      replyTo ! map.get(bearer)
      Behaviors.same
    case Command.Create(replyTo) =>
      val bearer      = Bearer.create()
      val sessionInfo = SessionInfo(bearer, SessionId.create())
      replyTo ! sessionInfo
      sessions(map + (bearer -> sessionInfo))
    case Command.Delete(bearer) =>
      sessions(map - bearer)
  }

}
