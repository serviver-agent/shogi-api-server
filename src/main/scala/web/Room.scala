package web

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Room {

  case class Message(body: String)

  sealed trait Command
  final case class AddMessage(message: Message)                       extends Command
  final case class FetchMessages(replyTo: ActorRef[List[Message]])    extends Command
  final case class SubscribeMessage(subscribeTo: ActorRef[Message])   extends Command
  final case class UnSubscribeMessage(subscribeTo: ActorRef[Message]) extends Command

  def apply(): Behavior[Command] = room(Nil, Set.empty)

  def room(
      messages: List[Message],
      subscribing: Set[ActorRef[Message]]
  ): Behavior[Command] = Behaviors.receive { (context, command) =>
    command match {
      case AddMessage(message) => {
        subscribing.foreach(_ ! message)
        room(message :: messages, subscribing)
      }
      case FetchMessages(replyTo) => {
        replyTo ! messages
        Behaviors.same
      }
      case SubscribeMessage(subscribeTo) => {
        room(messages, subscribing + subscribeTo)
      }
      case UnSubscribeMessage(subscribeTo) => {
        room(messages, subscribing - subscribeTo)
      }
    }
  }

}
