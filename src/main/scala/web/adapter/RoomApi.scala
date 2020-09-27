package web.adapter

import java.util.UUID
import java.nio.charset.StandardCharsets
import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSource}
import akka.stream.OverflowStrategy
import akka.http.scaladsl.model.{HttpResponse, HttpRequest, Uri, HttpEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.AttributeKeys.webSocketUpgrade
import akka.http.scaladsl.model.ws.{TextMessage, BinaryMessage, Message}
import akka.util.Timeout
import akka.NotUsed
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser

import web.room._
import RoomApi._
import Requests._

class RoomApi(actor: ActorRef[Rooms.Command], requestHelper: RequestHelper) {

  def listRooms: Action[Request] = requestHelper.withEncoder[List[RoomResponse]] {
    implicit val timeout: Timeout = 1.seconds

    val ask: Flow[Request, List[Rooms.RoomInfo], NotUsed] = ActorFlow
      .ask(parallelism = 8)(ref = actor)(makeMessage = (_, ref) => Rooms.ListRooms(ref))

    ask.map(_.map(info => RoomResponse(info.id.asString, info.roomName)))
  }

  def createRoom: Action[Request] = requestHelper.withCodec[CreateRoomRequest, RoomResponse] {
    implicit val timeout: Timeout = 1.seconds

    val flow: Flow[CreateRoomRequest, Rooms.RoomInfo, NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
      makeMessage = (message, ref) => Rooms.CreateRoom(message.name, ref)
    )

    flow.map { info => RoomResponse(info.id.asString, info.roomName) }
  }

  def deleteRoom(_roomId: String): Action[Request] = Flow.fromFunction { _ =>
    val roomId = Rooms.RoomId.fromString(_roomId)
    actor ! Rooms.DeleteRoom(roomId)
    HttpResponse(202)
  }

  def fetchMessages(_roomId: String): Action[Request] = {
    implicit val timeout: Timeout = 1.seconds

    val roomId = Rooms.RoomId.fromString(_roomId)

    val flow: Flow[Request, List[Room.Message], NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
      makeMessage = (_, ref) => Rooms.RoomCommand(roomId, Room.FetchMessages(ref))
    )

    val responseMapping: Flow[List[Room.Message], HttpResponse, NotUsed] =
      Flow[List[Room.Message]].map { messages =>
        val messageResponse =
          messages.map(m => MessageResponse(m.body))
        val responseString = encodeFetchMessageResponse(messageResponse)
        HttpResponse(200, entity = HttpEntity(responseString))
      }

    flow.via(responseMapping)
  }

  def addMessage(_roomId: String): Action[Request] = {
    val roomId = Rooms.RoomId.fromString(_roomId)
    val request: Flow[Request, Either[HttpResponse, (Rooms.RoomId, AddMessageRequest)], NotUsed] =
      Flow[Request].flatMapConcat { req =>
        val addMessageRequest: Source[Either[HttpResponse, AddMessageRequest], _] =
          req.http.entity.dataBytes.map(_.decodeString(StandardCharsets.UTF_8)).map(decodeAddMessageRequest)
        addMessageRequest.flatMapConcat {
          case Right(value) => Source.single(Right((roomId, value)))
          case Left(value)  => Source.single(Left(value))
        }
      }

    val flow: Flow[(Rooms.RoomId, AddMessageRequest), HttpResponse, NotUsed] = Flow.fromFunction {
      case (chatRoomId, request) =>
        actor ! Rooms.RoomCommand(chatRoomId, Room.AddMessage(Room.Message(request.body)))
        HttpResponse(202)
    }

    request.flatMapConcat {
      case Right(value) => Source.single(value).via(flow)
      case Left(value)  => Source.single(value)
    }
  }

  def subscribeMessage(_roomId: String): Action[Request] =
    requestHelper.websocket { request =>
      val chatRoomId = Rooms.RoomId.fromString(_roomId)

      val source = ActorSource
        .actorRef[Room.Message](
          completionMatcher = PartialFunction.empty,
          failureMatcher = PartialFunction.empty,
          bufferSize = 8,
          overflowStrategy = OverflowStrategy.fail
        )
        .mapMaterializedValue { subscribeTo =>
          actor ! Rooms.RoomCommand(chatRoomId, Room.SubscribeMessage(subscribeTo))
        }

      val responseMapping: Flow[Room.Message, TextMessage, NotUsed] = Flow.fromFunction { message =>
        TextMessage(encodeMessageResponse(MessageResponse(message.body)))
      }

      val requestMapping: Flow[Message, Room.Message, NotUsed] = Flow[Message]
        .flatMapConcat {
          case tm: TextMessage   => tm.textStream
          case bm: BinaryMessage => Source.empty
        }
        .map(body => Room.Message(body))

      val addMessage: Sink[Room.Message, NotUsed] = Sink
        .foreach[Room.Message] { message => actor ! Rooms.RoomCommand(chatRoomId, Room.AddMessage(message)) }
        .mapMaterializedValue(_ => NotUsed)

      Flow.fromSinkAndSource(
        requestMapping.to(addMessage),
        source.via(responseMapping)
      )
    }

}

object RoomApi {

  case class CreateRoomRequest(name: String)
  case class RoomResponse(chatRoomId: String, name: String)
  private val createRoomDecoder: Decoder[CreateRoomRequest] = Decoder.instance { c =>
    for {
      name <- c.downField("name").as[String]
    } yield CreateRoomRequest(name)
  }
  def decodeCreateRoomRequest(in: String): Either[HttpResponse, CreateRoomRequest] = {
    parser.parse(in).flatMap(createRoomDecoder.decodeJson).left.map { _ =>
      HttpResponse(400, entity = HttpEntity("bad request. invalid json."))
    }
  }
  private val roomEncoder: Encoder[RoomResponse] = Encoder.instance { a =>
    Json.obj(
      "chatRoomId" -> Json.fromString(a.chatRoomId),
      "name"       -> Json.fromString(a.name)
    )
  }
  def encodeCreateRoomResponse(res: RoomResponse): String = roomEncoder(res).noSpaces

  implicit val adapterCreateRoomDecoder: web.adapter.Decoder[CreateRoomRequest] = { req =>
    parser.parse(req).flatMap(createRoomDecoder.decodeJson).toOption
  }

  implicit val adapterCreateRoomEncoder: web.adapter.Encoder[RoomResponse] = { res => roomEncoder(res).noSpaces }

  implicit val adapterListRoomsEncoder: web.adapter.Encoder[List[RoomResponse]] = { res =>
    Encoder.encodeList(roomEncoder)(res).noSpaces
  }

  case class AddMessageRequest(body: String)
  private val addMessageDecoder: Decoder[AddMessageRequest] = Decoder.instance { c =>
    for {
      body <- c.downField("body").as[String]
    } yield AddMessageRequest(body)
  }
  def decodeAddMessageRequest(in: String): Either[HttpResponse, AddMessageRequest] = {
    parser.parse(in).flatMap(addMessageDecoder.decodeJson).left.map { _ =>
      HttpResponse(400, entity = HttpEntity("bad request. invalid json."))
    }
  }
  case class MessageResponse(body: String)
  private val messageEncoder: Encoder[MessageResponse] = Encoder.instance { a =>
    Json.obj(
      "body" -> Json.fromString(a.body)
    )
  }
  def encodeMessageResponse(res: MessageResponse): String = {
    messageEncoder(res).noSpaces
  }
  def encodeFetchMessageResponse(res: List[MessageResponse]): String = {
    Encoder.encodeList(messageEncoder)(res).noSpaces
  }

}
