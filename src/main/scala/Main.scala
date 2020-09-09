import web.WebApp
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed
import scala.io.StdIn

object Main extends App {

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      context.spawn(WebApp(), "webapp")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

  val system = ActorSystem(Main(), "main")

  println("Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  system.terminate()

}
