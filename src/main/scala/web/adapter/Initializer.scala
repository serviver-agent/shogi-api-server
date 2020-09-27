package web.adapter

import web.room._
import web.session._
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.NotUsed
import scala.io.StdIn

object Initializer {

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val roomsActor    = context.spawn(Rooms(), "rooms")
      val sessionsActor = context.spawn(Sessions(), "sessions")
      val requestHelper = new RequestHelper(sessionsActor, Materializer.matFromSystem(context.system.classicSystem))
      val roomApi       = new RoomApi(roomsActor, requestHelper)
      val sessionsApi   = new SessionsApi(sessionsActor, requestHelper)
      val apis          = new ApisImpl(sessionsApi, roomApi, new ShogiMock)
      val router        = new RouterImpl(apis)
      context.spawn(WebServer(router), "webserver")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

}
