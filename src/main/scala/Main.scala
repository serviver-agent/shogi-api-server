import web._
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed
import scala.io.StdIn

object Main extends App {

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val roomsActor = context.spawn(Rooms(), "rooms")
      val roomApp    = new RoomApp(roomsActor)
      val webApp     = new WebApp(roomApp, new ShogiMock)
      val routes     = new Routes(webApp)
      context.spawn(WebServer(routes), "webserver")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

  val system = ActorSystem(Main(), "main")

  println("\nServer online at http://localhost:8080/\nPress RETURN to stop...\n")
  StdIn.readLine()
  system.terminate()

}
