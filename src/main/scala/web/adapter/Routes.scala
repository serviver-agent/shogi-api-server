package web.adapter

import scala.util.matching.Regex
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, HttpMethod, Uri, HttpEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.NotUsed

import web.session.SessionId
import Requests._

import Requests.{SimpleRequest, Action}

trait Router {
  protected val route: PartialFunction[(HttpMethod, Uri.Path), Action[Request]]

  final def handleRequest(implicit mat: Materializer): Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { (httpRequest: HttpRequest) =>
      val method = httpRequest.method
      val uri    = httpRequest.uri.path

      if (route.isDefinedAt((method, uri))) {
        val request                                    = SimpleRequest(httpRequest)
        val flow: Flow[Request, HttpResponse, NotUsed] = route(method, uri)
        Source.single(request).via(flow)
      } else {
        httpRequest.discardEntityBytes()
        Source.single(HttpResponse(404, entity = HttpEntity("Unknown resource!")))
      }

    }
  }
}

class RouterImpl(apis: Apis) extends Router {

  object Root extends Router {
    val Root = "/"
    val route = {
      case GET -> Uri.Path(Root) =>
        Flow.fromFunction((_: Request) => HttpResponse(200, entity = HttpEntity("shogi-api-server")))
    }
  }

  object Rooms extends Router {
    private val UUID     = """([\da-fA-F]{8}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{4}-[\da-fA-F]{12})"""
    val ListRooms        = "/rooms"
    val CreateRoom       = "/rooms"
    val DeleteRoom       = s"""/rooms/$UUID""".r
    val FetchMessages    = s"""/rooms/$UUID""".r
    val AddMessage       = s"""/rooms/$UUID""".r
    val SubscribeMessage = s"""/rooms/$UUID/stream""".r

    val route = {
      case GET -> Uri.Path(ListRooms)            => apis.roomApi.listRooms
      case POST -> Uri.Path(CreateRoom)          => apis.roomApi.createRoom
      case DELETE -> Uri.Path(DeleteRoom(id))    => apis.roomApi.deleteRoom(id)
      case GET -> Uri.Path(FetchMessages(id))    => apis.roomApi.fetchMessages(id)
      case POST -> Uri.Path(AddMessage(id))      => apis.roomApi.addMessage(id)
      case GET -> Uri.Path(SubscribeMessage(id)) => apis.roomApi.subscribeMessage(id)
    }
  }

  object Sessions extends Router {
    val Sessions = "/sessions"

    val route = {
      case POST -> Uri.Path(Sessions)   => apis.sessionsApi.createSession
      case GET -> Uri.Path(Sessions)    => apis.sessionsApi.getSession
      case DELETE -> Uri.Path(Sessions) => apis.sessionsApi.deleteSession
    }
  }

  val route = Root.route
    .orElse(Rooms.route)
    .orElse(Sessions.route)

}
