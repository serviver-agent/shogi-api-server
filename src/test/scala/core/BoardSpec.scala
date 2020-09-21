package core

import core.Area.{A1, A2, A3, A4, B1, B2, B3, B4, C1, C2, C3, C4}
import core.Koma.{Hiyoko, Kirin, Lion, Zou}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardSpec extends AnyFlatSpec with Matchers {
  "Board" should "盤面を初期状態にできる" in {
    val actual = Board.factory
    val expected = Board(
      a1 = Masu(A1, Some(Kirin(Gote))),
      a2 = Masu(A2, None),
      a3 = Masu(A3, None),
      a4 = Masu(A4, Some(Zou(Sente))),
      b1 = Masu(B1, Some(Lion(Gote))),
      b2 = Masu(B2, Some(Hiyoko(Gote))),
      b3 = Masu(B3, Some(Hiyoko(Sente))),
      b4 = Masu(B4, Some(Lion(Sente))),
      c1 = Masu(C1, Some(Zou(Gote))),
      c2 = Masu(C2, None),
      c3 = Masu(C3, None),
      c4 = Masu(C4, Some(Kirin(Sente))),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )

    actual shouldEqual expected
  }

  it should "実際の駒より多い駒が存在する盤面はassertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        a1 = Masu(A1, None),
        a2 = Masu(A2, None),
        a3 = Masu(A3, None),
        a4 = Masu(A4, Some(Zou(Sente))),
        b1 = Masu(B1, Some(Lion(Gote))),
        b2 = Masu(B2, Some(Hiyoko(Gote))),
        b3 = Masu(B3, Some(Hiyoko(Sente))),
        b4 = Masu(B4, Some(Lion(Sente))),
        c1 = Masu(C1, Some(Zou(Gote))),
        c2 = Masu(C2, None),
        c3 = Masu(C3, None),
        c4 = Masu(C4, Some(Kirin(Sente))),
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
        a1 = Masu(A1, None),
        a2 = Masu(A2, None),
        a3 = Masu(A3, None),
        a4 = Masu(A4, Some(Zou(Sente))),
        b1 = Masu(B1, Some(Lion(Gote))),
        b2 = Masu(B2, Some(Hiyoko(Gote))),
        b3 = Masu(B3, Some(Hiyoko(Sente))),
        b4 = Masu(B4, Some(Lion(Sente))),
        c1 = Masu(C1, Some(Zou(Gote))),
        c2 = Masu(C2, None),
        c3 = Masu(C3, None),
        c4 = Masu(C4, Some(Kirin(Sente))),
        senteKomadai = Komadai(
          Sente,
          Seq(Kirin(Sente), Kirin(Sente), Zou(Sente), Zou(Sente), Lion(Sente), Lion(Sente), Hiyoko(Sente))
        ),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }
}
