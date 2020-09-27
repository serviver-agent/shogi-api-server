package web.adapter

import java.nio.charset.StandardCharsets

import scala.concurrent.duration._

import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.typed.scaladsl.ActorFlow
import akka.stream.scaladsl.{Flow, Source}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, HttpEntity, ContentTypes}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.AttributeKeys.webSocketUpgrade
import akka.util.{ByteString, Timeout}
import akka.NotUsed

import web.session._
import web.session.Sessions.SessionInfo
import Requests._
import RequestHelper._

class RequestHelper(
    actor: ActorRef[Sessions.Command],
    mat: Materializer
) {

  def withCodec[A, B](
      flow: Flow[A, B, NotUsed]
  )(implicit decoder: Decoder[A], encoder: Encoder[B]): Flow[Request, HttpResponse, NotUsed] = {
    Flow[Request].flatMapConcat { req =>
      val sourceOptA: Source[Option[A], Any] =
        req.http.entity.dataBytes.map(_.decodeString(StandardCharsets.UTF_8)).map(decoder.decode)
      sourceOptA.flatMapConcat {
        case None => Source.single(Responses.InvalidRequestJson)
        case Some(a) =>
          Source.single(a).via(flow).map { b =>
            val byteString = ByteString.apply(encoder.encode(b), StandardCharsets.UTF_8)
            HttpResponse(200, entity = HttpEntity.apply(ContentTypes.`application/json`, encoder.encode(b)))
          }
      }
    }
  }

  def withEncoder[B](
      flow: Flow[Request, B, NotUsed]
  )(implicit encoder: Encoder[B]): Flow[Request, HttpResponse, NotUsed] = {
    Flow[Request].via(flow).map { b =>
      val byteString = ByteString.apply(encoder.encode(b), StandardCharsets.UTF_8)
      HttpResponse(200, entity = HttpEntity.apply(ContentTypes.`application/json`, encoder.encode(b)))
    }
  }

  def withSession(flow: Flow[SessionRequest, HttpResponse, NotUsed]): Flow[Request, HttpResponse, NotUsed] = {
    implicit val timeout: Timeout = 100.minutes

    Flow[Request].flatMapConcat { request =>
      val bearer: Either[HttpResponse, Bearer] = parseBearer(request.http)

      val ask: Flow[Bearer, Option[SessionInfo], NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
        makeMessage = (bearer, ref) => Sessions.Command.Get(bearer, ref)
      )

      val response: Flow[Option[SessionInfo], Either[HttpResponse, SessionRequest], NotUsed] = Flow.fromFunction {
        case None              => Left(Responses.TokenExpired)
        case Some(sessionInfo) => Right(new SessionRequest(sessionInfo.sessionId, request.http))
      }

      bearer match {
        case Left(response) => Source.single(response)
        case Right(bearer) =>
          Source.single(bearer).via(ask).via(response).flatMapConcat {
            case Left(response)        => Source.single(response)
            case Right(sessionRequest) => Source.single(sessionRequest).via(flow)
          }
      }
    }
  }

  def websocket[A <: Request](proc: WebSocket[A]): Action[A] = Flow[A].flatMapConcat { req =>
    val flow = req.http.attribute(webSocketUpgrade) match {
      case Some(upgrade) =>
        Flow.fromFunction[A, HttpResponse](req => upgrade.handleMessages(proc(req)))
      case None =>
        Flow.fromFunction((_: A) => HttpResponse(400, entity = HttpEntity("Not a valid websocket request!")))
    }
    Source.single(req).via(flow)
  }
}

object RequestHelper {

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

    val InvalidRequestJson =
      HttpResponse(400, entity = HttpEntity("invalid json"))
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
