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
    import JsonCodec._
    import io.circe.syntax._
    Shiai.init.asJson.spaces2
  }

  object JsonCodec {

    import core._
    import io.circe._
    import io.circe.syntax._

    implicit lazy val komaEncoder: Encoder[Koma] = new Encoder[Koma] {
      override def apply(a: Koma): Json = a match {
        case Koma.Lion   => "Lion".asJson
        case Koma.Kirin  => "Kirin".asJson
        case Koma.Zou    => "Zou".asJson
        case Koma.Hiyoko => "Hiyoko".asJson
      }
    }

    implicit lazy val locationEncoder: Encoder[Location] = new Encoder[Location] {
      override def apply(a: Location): Json = a match {
        case Location.OnPlayer1Tegoma =>
          Json.obj(
            "tpe" -> "onPlayer1Tegoma".asJson
          )
        case Location.OnPlayer2Tegoma =>
          Json.obj(
            "tpe" -> "onPlayer1Tegoma".asJson
          )
        case Location.OnShogiban(yoko, tate) =>
          Json.obj(
            "tpe"  -> "onShogiban".asJson,
            "yoko" -> yoko.n.asJson,
            "tate" -> tate.n.asJson
          )
      }
    }

    implicit lazy val komaStatusEncoder: Encoder[KomaStatus] = new Encoder[KomaStatus] {
      override def apply(a: KomaStatus): Json = Json.obj(
        "koma"     -> a.koma.asJson,
        "loaction" -> a.location.asJson
      )
    }

    implicit lazy val shogibanEncoder: Encoder[Shogiban] = new Encoder[Shogiban] {
      override def apply(a: Shogiban): Json = Json.obj(
        "lion1"   -> a.lion1.asJson,
        "lion2"   -> a.lion2.asJson,
        "kirin1"  -> a.kirin1.asJson,
        "kirin2"  -> a.kirin2.asJson,
        "zou1"    -> a.zou1.asJson,
        "zou2"    -> a.zou2.asJson,
        "hiyoko1" -> a.hiyoko1.asJson,
        "hiyoko2" -> a.hiyoko2.asJson
      )
    }

    implicit lazy val shiaiStatusEncoder: Encoder[Shiai.ShiaiStatus] = new Encoder[Shiai.ShiaiStatus] {
      override def apply(a: Shiai.ShiaiStatus): Json = a match {
        case Shiai.ShiaiStatus.Player1 => "Player1".asJson
        case Shiai.ShiaiStatus.Player2 => "Player2".asJson
        case Shiai.ShiaiStatus.Finish  => "Finish".asJson
      }
    }

    implicit lazy val shiaiEncoder: Encoder[Shiai] = new Encoder[Shiai] {
      override def apply(a: Shiai): Json = Json.obj(
        "shogiban"    -> a.shogiban.asJson,
        "shiaiStatus" -> a.shiaiStatus.asJson
      )
    }

  }

}
