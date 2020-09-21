package core

import core.Area.{A1, A2, A3, A4, B1, B2, B3, B4, C1, C2, C3, C4}
import core.Koma.{Hiyoko, Kirin, Lion, Zou}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.language.implicitConversions

class BoardSpec extends AnyFlatSpec with Matchers {
  "Board" should "盤面を初期状態にできる" in {
    val actual = Board.factory
    val expected = Board(
      masus = Set(
        Masu(A1, Some(Kirin(Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(Zou(Sente))),
        Masu(B1, Some(Lion(Gote))),
        Masu(B2, Some(Hiyoko(Gote))),
        Masu(B3, Some(Hiyoko(Sente))),
        Masu(B4, Some(Lion(Sente))),
        Masu(C1, Some(Zou(Gote))),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(Kirin(Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )

    actual shouldEqual expected
  }

  it should "実際の駒より多い駒が存在する盤面はassertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        masus = Set(
          Masu(A1, None),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(Zou(Sente))),
          Masu(B1, Some(Lion(Gote))),
          Masu(B2, Some(Hiyoko(Gote))),
          Masu(B3, Some(Hiyoko(Sente))),
          Masu(B4, Some(Lion(Sente))),
          Masu(C1, Some(Zou(Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(Kirin(Sente)))
        ),
        senteKomadai = Komadai(
          Sente,
          Seq(
            Kirin(Sente),
            Kirin(Sente),
            Zou(Sente),
            Zou(Sente),
            Lion(Sente),
            Lion(Sente),
            Hiyoko(Sente),
            Hiyoko(Sente),
            Hiyoko(Sente)
          )
        ),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }

  it should "実際の駒より少ない駒が存在する盤面はassertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        masus = Set(
          Masu(A1, None),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(Zou(Sente))),
          Masu(B1, Some(Lion(Gote))),
          Masu(B2, Some(Hiyoko(Gote))),
          Masu(B3, Some(Hiyoko(Sente))),
          Masu(B4, Some(Lion(Sente))),
          Masu(C1, Some(Zou(Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(Kirin(Sente)))
        ),
        senteKomadai = Komadai(
          Sente,
          Seq(Kirin(Sente), Kirin(Sente), Zou(Sente), Zou(Sente), Lion(Sente), Lion(Sente), Hiyoko(Sente))
        ),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }
}
