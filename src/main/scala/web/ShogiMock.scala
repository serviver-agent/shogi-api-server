package web

import core.Shiai

import akka.stream.scaladsl.{Source, Flow}
import akka.http.scaladsl.model.{HttpResponse, Uri, HttpRequest, HttpEntity, ContentTypes}
import akka.http.scaladsl.model.HttpMethods._
import akka.NotUsed

class ShogiMock {

  def handleRequest: Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { r: HttpRequest =>
      val flow = r match {
        case req @ HttpRequest(GET, Uri.Path("/shogi"), _, _, _) =>
          Flow.fromFunction((_: HttpRequest) =>
            HttpResponse(200, entity = HttpEntity(ContentTypes.`application/json`, shogiJson()))
          )
        // FIXME: handle other path to 404 error
      }
      Source.single(r).via(flow)
    }
  }

  def shogiJson(): String = {
    JsonCodec.shiaiEncoder(Shiai.init).spaces2
  }

  object JsonCodec {

    import core._
    import io.circe._

    val playerEncoder: Encoder[Player] = Encoder.instance {
      case Sente => Json.fromString("Sente")
      case Gote  => Json.fromString("Gote")
    }

    val komaEncoder: Encoder[Koma] = Encoder.instance { koma =>
      val tpe = koma match {
        case Koma.Lion(_)     => "Lion"
        case Koma.Kirin(_)    => "Kirin"
        case Koma.Zou(_)      => "Zou"
        case Koma.Hiyoko(_)   => "Hiyoko"
        case Koma.Niwatori(_) => "Niwatori"
      }
      Json.obj(
        "tpe"    -> Json.fromString(tpe),
        "player" -> playerEncoder(koma.player)
      )
    }

    val masuEncoder: Encoder[Masu[_]] = Encoder.instance { masu =>
      masu.maybeKoma match {
        case None       => Json.Null
        case Some(koma) => komaEncoder(koma)
      }
    }

    val komadaiEncoder: Encoder[Komadai[_]] = Encoder.instance { komadai =>
      Json.fromValues(komadai.komas.map(komaEncoder.apply))
    }

    val boardEncoder: Encoder[Board] = Encoder.instance { board =>
      Json.obj(
        "a1"           -> masuEncoder(board.a1),
        "a2"           -> masuEncoder(board.a2),
        "a3"           -> masuEncoder(board.a3),
        "a4"           -> masuEncoder(board.a4),
        "b1"           -> masuEncoder(board.b1),
        "b2"           -> masuEncoder(board.b2),
        "b3"           -> masuEncoder(board.b3),
        "b4"           -> masuEncoder(board.b4),
        "c1"           -> masuEncoder(board.c1),
        "c2"           -> masuEncoder(board.c2),
        "c3"           -> masuEncoder(board.c3),
        "c4"           -> masuEncoder(board.c4),
        "senteKomadai" -> komadaiEncoder(board.senteKomadai),
        "goteKomadai"  -> komadaiEncoder(board.goteKomadai)
      )
    }

    val statusEncoder: Encoder[Shiai.Status] = Encoder.instance {
      case Shiai.Status.Player1 => Json.fromString("Player1")
      case Shiai.Status.Player2 => Json.fromString("Player2")
      case Shiai.Status.Finish  => Json.fromString("Finish")
    }

    val shiaiEncoder: Encoder[Shiai] = Encoder.instance { a =>
      Json.obj(
        "board"  -> boardEncoder(a.board),
        "status" -> statusEncoder(a.status)
      )
    }

  }

}
