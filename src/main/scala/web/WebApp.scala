package web

import core.Shiai

import scala.concurrent.Future
import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, Uri, HttpRequest, HttpEntity, ContentTypes}
import akka.NotUsed

object WebApp {

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      implicit val system = context.system.classicSystem
      implicit val ec     = system.dispatcher

      val requestHandler: HttpRequest => HttpResponse = {
        case req @ HttpRequest(GET, Uri.Path("/shogi"), _, _, _) =>
          HttpResponse(200, entity = HttpEntity(ContentTypes.`application/json`, shogiJson()))
        case req @ HttpRequest(GET, Uri.Path("/greeter"), _, _, _) =>
          HttpResponse(200, entity = "OK!")
        case r: HttpRequest =>
          r.discardEntityBytes() // important to drain incoming HTTP Entity stream
          HttpResponse(404, entity = "Unknown resource!")
      }

      val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
        Http().newServerAt("localhost", 8080).connectionSource()
      val bindingFuture: Future[Http.ServerBinding] =
        serverSource
          .to(Sink.foreach { connection => // foreach materializes the source
            println("Accepted new connection from " + connection.remoteAddress)
            connection handleWithSyncHandler requestHandler
          })
          .run()

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          bindingFuture.flatMap(_.unbind())
          Behaviors.stopped
      }
    }
  }

  def shogiJson(): String = {
    import web.JsonCodec._
    import io.circe.syntax._
    Shiai.init.asJson.spaces2
  }

}
