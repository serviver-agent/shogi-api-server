package web

import akka.http.scaladsl.model.{HttpResponse, Uri, HttpRequest}
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.NotUsed
import akka.stream.Materializer

// TODO: remove this import
// implicit conversion String => ResponseEntity
// ex:
// HttpResponse(200, entity = "shogi-api-server"))
//                            ^^^^^^^^^^^^^^^^^^
import scala.language.implicitConversions

class Routes(webApp: WebApp) {

  def requestHandler(implicit mat: Materializer): Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow[HttpRequest].flatMapConcat { (r: HttpRequest) =>
      {
        val flow = r match {
          case req @ HttpRequest(_, Uri.Path("/"), _, _, _) =>
            Flow.fromFunction((_: HttpRequest) => HttpResponse(200, entity = "shogi-api-server"))
          case req @ HttpRequest(_, Uri.Path(path), _, _, _) if path.startsWith("/shogi") =>
            webApp.shogiApp.handleRequest
          case req @ HttpRequest(_, Uri.Path(path), _, _, _) if path.startsWith("/room") => webApp.roomApp.handleRequest
          case req @ _ => {
            r.discardEntityBytes()
            Flow.fromSinkAndSource(
              sink = Sink.ignore,
              source = Source.single(HttpResponse(404, entity = "Unknown resource!"))
            )
          }
        }
        Source.single(r).via(flow)
      }
    }

}
