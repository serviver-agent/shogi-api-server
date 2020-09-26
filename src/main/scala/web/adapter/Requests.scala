package web.adapter

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.ws.Message
import akka.NotUsed

import web.session.SessionId

object Requests {
  trait Request {
    def http: HttpRequest
  }
  case class SimpleRequest(http: HttpRequest)                        extends Request
  case class SessionRequest(sessionId: SessionId, http: HttpRequest) extends Request

  type Action[A <: Request]    = Flow[A, HttpResponse, NotUsed]
  type WebSocket[A <: Request] = A => Flow[Message, Message, NotUsed]
}
