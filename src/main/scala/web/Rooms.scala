package web

import java.util.UUID
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Rooms {

  sealed trait Command
  final case class CreateRoom(roomName: String, replyTo: ActorRef[RoomInfo]) extends Command
  final case class ListRooms(replyTo: ActorRef[List[RoomInfo]])              extends Command
  final case class DeleteRoom(roomId: RoomId)                                extends Command
  final case class RoomCommand(roomId: RoomId, command: Room.Command)        extends Command

  case class RoomId(value: UUID) {
    def asString: String = value.toString
  }
  object RoomId {
    def create(): RoomId               = RoomId(UUID.randomUUID())
    def fromString(id: String): RoomId = RoomId(UUID.fromString(id))
  }

  case class RoomInfo(id: RoomId, roomName: String, ref: ActorRef[Room.Command])

  def apply(): Behavior[Command] = rooms(Map.empty)

  def rooms(
      roomsMap: Map[RoomId, RoomInfo]
  ): Behavior[Command] = Behaviors.receive { (context, command) =>
    command match {
      case CreateRoom(roomName, replyTo) => {
        val chatRoomId   = RoomId.create()
        val chatRoomRef  = context.spawnAnonymous(Room())
        val chatRoomInfo = RoomInfo(chatRoomId, roomName, chatRoomRef)
        replyTo ! chatRoomInfo
        rooms(roomsMap + (chatRoomId -> chatRoomInfo))
      }
      case ListRooms(replyTo) => {
        replyTo ! roomsMap.values.toList
        Behaviors.same
      }
      case DeleteRoom(chatRoomId) => {
        rooms(roomsMap - chatRoomId)
      }
      case RoomCommand(chatRoomId, command) => {
        roomsMap.get(chatRoomId).map(_.ref).foreach(_ ! command) // FIXME roomIdが存在しなかった時の挙動
        Behaviors.same
      }
    }
  }

}
