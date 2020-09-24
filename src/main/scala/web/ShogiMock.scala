package web

import core.Shiai

import akka.stream.scaladsl.{Source, Flow}
import akka.http.scaladsl.model.{HttpResponse, Uri, HttpRequest, HttpEntity, ContentTypes}
import akka.http.scaladsl.model.HttpMethods._
import akka.NotUsed

class ShogiMock {

  def handleRequest: Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { (r: HttpRequest) =>
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

    val komaEncoder: Encoder[Koma] = Encoder.instance {
      case Koma.Lion     => Json.fromString("Lion")
      case Koma.Kirin    => Json.fromString("Kirin")
      case Koma.Zou      => Json.fromString("Zou")
      case Koma.Hiyoko   => Json.fromString("Hiyoko")
      case Koma.Niwatori => Json.fromString("Niwatori")
    }

    val areaEncoder: Encoder[Area] = Encoder.instance { area =>
      Json.obj(
        "x" -> Json.fromInt(area.x),
        "y" -> Json.fromInt(area.y)
      )
    }

    val masuEncoder: Encoder[Masu] = Encoder.instance { masu =>
      val koma = masu.upon match {
        case None => Json.Null
        case Some(playersKoma) =>
          Json.obj(
            "player" -> playerEncoder(playersKoma.player),
            "koma"   -> komaEncoder(playersKoma.koma)
          )
      }
      Json.obj(
        "area" -> areaEncoder(masu.area),
        "koma" -> koma
      )
    }

    val komadaiEncoder: Encoder[Komadai[_]] = Encoder.instance { komadai =>
      Json.fromValues(komadai.komas.map(komaEncoder.apply))
    }

    val boardEncoder: Encoder[Board] = Encoder.instance { board =>
      Json.obj(
        "masus"        -> Encoder.encodeList(masuEncoder)(board.masus.toList),
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
