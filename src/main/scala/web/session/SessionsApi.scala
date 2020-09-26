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
import web.adapter.Requests._
import web.adapter.RequestHelper

class SessionsApi(actor: ActorRef[Sessions.Command], requestHepler: RequestHelper) {

  val createSession: Action[Request] = {
    implicit val timeout: Timeout = 100.minutes

    val ask: Flow[Request, SessionInfo, NotUsed] = ActorFlow.ask(parallelism = 8)(ref = actor)(
      makeMessage = (_, ref) => Sessions.Command.Create(ref)
    )

    ask.map { sessionInfo =>
      HttpResponse(200, headers = RawHeader("X-Authenticate", sessionInfo.bearer.asString) :: Nil)
    }
  }

  val getSession: Action[Request] = requestHepler.withSession {
    Flow.fromFunction { (request: SessionRequest) =>
      HttpResponse(200, entity = HttpEntity(s"sessionId: ${request.sessionId.asString}"))
    }
  }

  val deleteSession: Action[Request] = Flow.fromFunction { (request: Request) =>
    RequestHelper
      .parseBearer(request.http)
      .map { bearer =>
        actor ! Sessions.Command.Delete(bearer)
        HttpResponse(202)
      }
      .merge
  }

}
