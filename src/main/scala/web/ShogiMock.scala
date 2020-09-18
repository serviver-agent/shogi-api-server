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

    val komaEncoder: Encoder[Koma] = Encoder.instance {
      case Koma.Lion   => Json.fromString("Lion")
      case Koma.Kirin  => Json.fromString("Kirin")
      case Koma.Zou    => Json.fromString("Zou")
      case Koma.Hiyoko => Json.fromString("Hiyoko")
    }

    val locationEncoder: Encoder[Location] = Encoder.instance {
      case Location.OnPlayer1Tegoma =>
        Json.obj(
          "tpe" -> Json.fromString("onPlayer1Tegoma")
        )
      case Location.OnPlayer2Tegoma =>
        Json.obj(
          "tpe" -> Json.fromString("onPlayer1Tegoma")
        )
      case Location.OnShogiban(yoko, tate) =>
        Json.obj(
          "tpe"  -> Json.fromString("onShogiban"),
          "yoko" -> Json.fromInt(yoko.n),
          "tate" -> Json.fromInt(tate.n)
        )
    }

    val komaStatusEncoder: Encoder[KomaStatus] = Encoder.instance { a =>
      Json.obj(
        "koma"     -> komaEncoder(a.koma),
        "loaction" -> locationEncoder(a.location)
      )
    }

    val shogibanEncoder: Encoder[Shogiban] = Encoder.instance { a =>
      Json.obj(
        "lion1"   -> komaStatusEncoder(a.lion1),
        "lion2"   -> komaStatusEncoder(a.lion2),
        "kirin1"  -> komaStatusEncoder(a.kirin1),
        "kirin2"  -> komaStatusEncoder(a.kirin2),
        "zou1"    -> komaStatusEncoder(a.zou1),
        "zou2"    -> komaStatusEncoder(a.zou2),
        "hiyoko1" -> komaStatusEncoder(a.hiyoko1),
        "hiyoko2" -> komaStatusEncoder(a.hiyoko2)
      )
    }

    val shiaiStatusEncoder: Encoder[Shiai.ShiaiStatus] = Encoder.instance {
      case Shiai.ShiaiStatus.Player1 => Json.fromString("Player1")
      case Shiai.ShiaiStatus.Player2 => Json.fromString("Player2")
      case Shiai.ShiaiStatus.Finish  => Json.fromString("Finish")
    }

    val shiaiEncoder: Encoder[Shiai] = Encoder.instance { a =>
      Json.obj(
        "shogiban"    -> shogibanEncoder(a.shogiban),
        "shiaiStatus" -> shiaiStatusEncoder(a.shiaiStatus)
      )
    }

  }

}
