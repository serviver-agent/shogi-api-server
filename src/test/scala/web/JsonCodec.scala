package web

import core._
import web.JsonCodec._
import io.circe.syntax._
import io.circe.parser.parse

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JsonCodecSpec extends AnyFlatSpec with Matchers {
  "JsonCodec.shiaiEncoder" should "試合の初期状態をJsonにできる" in {
    val initialShiai = Shiai.init
    val resultJson   = parse("""
{
  "shogiban" : {
    "lion1" : {
      "koma" : "Lion",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 1,
        "tate" : 3
      }
    },
    "lion2" : {
      "koma" : "Lion",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 1,
        "tate" : 0
      }
    },
    "kirin1" : {
      "koma" : "Kirin",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 2,
        "tate" : 3
      }
    },
    "kirin2" : {
      "koma" : "Kirin",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 0,
        "tate" : 0
      }
    },
    "zou1" : {
      "koma" : "Zou",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 0,
        "tate" : 3
      }
    },
    "zou2" : {
      "koma" : "Zou",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 2,
        "tate" : 0
      }
    },
    "hiyoko1" : {
      "koma" : "Hiyoko",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 1,
        "tate" : 2
      }
    },
    "hiyoko2" : {
      "koma" : "Hiyoko",
      "loaction" : {
        "tpe" : "onShogiban",
        "yoko" : 1,
        "tate" : 1
      }
    }
  },
  "shiaiStatus" : "Player1"
}
    """).getOrElse(null)

    initialShiai.asJson shouldEqual resultJson
  }
}
