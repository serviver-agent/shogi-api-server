package web.session

import scala.concurrent.duration._

import akka.actor.typed.ActorRef
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.stream.typed.scaladsl.ActorFlow
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri, HttpEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.util.Timeout
import akka.NotUsed

import Sessions.SessionInfo
import SessionsApi._

class SessionsApi(actor: ActorRef[Sessions.Command]) {

  def handleRequest: Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { (r: HttpRequest) =>
      val flow = r match {
        case HttpRequest(GET, Uri.Path(SessionsApiUrl), _, _, _)    => getSession
        case HttpRequest(POST, Uri.Path(SessionsApiUrl), _, _, _)   => createSession
        case HttpRequest(DELETE, Uri.Path(SessionsApiUrl), _, _, _) => deleteSession
      }
      Source.single(r).via(flow)
    }
  }

  private val getSession: Flow[HttpRequest, HttpResponse, NotUsed] = {
    implicit val timeout: Timeout = 100.minutes

    Flow[HttpRequest].flatMapConcat { (request: HttpRequest) =>
      val bearer: Either[HttpResponse, Bearer] = parseBearer(request)

      val ask: Flow[Bearer, Option[SessionInfo], NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
        makeMessage = (bearer, ref) => Sessions.Command.Get(bearer, ref)
      )

      val response: Flow[Option[SessionInfo], HttpResponse, NotUsed] = Flow.fromFunction {
        case None              => Responses.TokenExpired
        case Some(sessionInfo) => HttpResponse(200, entity = HttpEntity(s"sessionId: ${sessionInfo.sessionId}"))
      }

      bearer match {
        case Left(response) => Source.single(response)
        case Right(bearer)  => Source.single(bearer).via(ask).via(response)
      }
    }
  }

  private val createSession: Flow[HttpRequest, HttpResponse, NotUsed] = {
    implicit val timeout: Timeout = 100.minutes

    val ask: Flow[HttpRequest, SessionInfo, NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
      makeMessage = (_, ref) => Sessions.Command.Create(ref)
    )

    ask.map(Responses.grantSession)
  }

  private val deleteSession: Flow[HttpRequest, HttpResponse, NotUsed] = Flow.fromFunction { (request: HttpRequest) =>
    parseBearer(request).map { bearer =>
      actor ! Sessions.Command.Delete(bearer)
      HttpResponse(202)
    }.merge
  }

}

object SessionsApi {

  val SessionsApiUrl        = "/sessions"
  val SessionsApiUrlMatcher = "/sessions(.*)".r

  object Responses {
    val AuthorizationHeaderNotIncluded =
      HttpResponse(401).withHeaders(
        `WWW-Authenticate`(HttpChallenge(scheme = "Bearer", realm = "token_required")) :: Nil
      )
    val InvalidRequestParameter =
      HttpResponse(400).withHeaders(
        `WWW-Authenticate`(HttpChallenge(scheme = "Bearer", realm = "invalid_request")) :: Nil
      )
    val TokenExpired =
      HttpResponse(401).withHeaders(
        `WWW-Authenticate`(HttpChallenge(scheme = "Bearer", realm = "invalid_token")) :: Nil
      )

    def grantSession(sessionInfo: SessionInfo) =
      HttpResponse(200, headers = RawHeader("X-Authenticate", sessionInfo.bearer.asString) :: Nil)
  }

  def parseBearer(request: HttpRequest): Either[HttpResponse, Bearer] =
    for {
      bearerStr <- request.headers
        .find(_.is("authorization"))
        .map(_.value)
        .toRight(Responses.AuthorizationHeaderNotIncluded)
      bearer <- Bearer
        .fromString(bearerStr)
        .toRight(Responses.InvalidRequestParameter)
    } yield bearer

}
