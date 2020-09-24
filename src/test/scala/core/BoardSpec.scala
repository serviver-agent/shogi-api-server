package core

import core.Area.{A1, A2, A3, A4, B1, B2, B3, B4, C1, C2, C3, C4}
import core.Koma.{Hiyoko, Kirin, Lion, Zou, Niwatori}
import core.Board.{MoveKomaRequest, MoveKomaError}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.language.implicitConversions

class BoardSpec extends AnyFlatSpec with Matchers {

  "Board" should "盤面を初期状態にできる" in {
    val actual = Board.factory
    val expected = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
        Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, Some(PlayersKoma(Zou, Gote))),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )

    actual shouldEqual expected
  }

  /* 駒の個数に関するバリデーション */

  it should "実際の駒より多い駒が存在する盤面はassertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        masus = Set(
          Masu(A1, None),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(
          Sente,
          Seq(Kirin, Kirin, Zou, Zou, Lion, Lion, Hiyoko, Hiyoko, Hiyoko)
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
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(
          Sente,
          Seq(Kirin, Kirin, Zou, Zou, Lion, Lion, Hiyoko)
        ),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }

  /* マスの重複に関するバリデーション */

  it should "Boardの中に同じAreaのMasuが存在するとAssertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A1, None), // !!
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }

  it should "Boardの中のMasuが少ないとAssertionErrorになる" in {
    assertThrows[AssertionError] {
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          // !!
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    }
  }

  /* コマの移動に関するバリデーション */

  it should "コマを移動できる(先手)" in {
    val init  = Board.factory
    val moved = init.moveKoma(MoveKomaRequest.Ugokasu(B3, B2, Sente, false))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Sente))), // !!
          Masu(B3, None),                             // !!
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq(Hiyoko)),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    )
    assert(moved == expected)
  }

  it should "コマを移動できる(後手)" in {
    val init  = Board.factory
    val moved = init.moveKoma(MoveKomaRequest.Ugokasu(B2, B3, Gote, false))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, None),
          Masu(B3, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq(Hiyoko))
      )
    )
    assert(moved == expected)
  }

  it should "存在しないコマは移動できない" in {
    val init     = Board.factory
    val moved    = init.moveKoma(MoveKomaRequest.Ugokasu(C2, B2, Sente, false))
    val expected = Left(MoveKomaError.FromKomaNotFound)
    assert(moved == expected)
  }

  it should "相手のコマは移動できない" in {
    val init     = Board.factory
    val moved    = init.moveKoma(MoveKomaRequest.Ugokasu(B1 /* 相手のライオン */, B2, Sente, false))
    val expected = Left(MoveKomaError.FromKomaIsNotOwnedByThatPlayer)
    assert(moved == expected)
  }

  it should "コマはそのコマが移動できる場所にしか移動できない" in {
    val init     = Board.factory
    val moved    = init.moveKoma(MoveKomaRequest.Ugokasu(B3 /* ヒヨコ */, C3, Sente, false))
    val expected = Left(MoveKomaError.KomahaSonobashoniIdouDekinai)
    assert(moved == expected)
  }

  /* 駒台から打つ */

  it should "駒台から駒を打てる(先手)" in {
    val init = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Zou, Gote))),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, None),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq(Hiyoko)),
      goteKomadai = Komadai(Gote, Seq(Hiyoko))
    )
    val moved = init.moveKoma(MoveKomaRequest.FromKomadai(Hiyoko, B3, Sente))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Zou, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, None),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq(Hiyoko))
      )
    )
    assert(moved == expected)
  }

  it should "駒台から駒を打てる(後手)" in {
    val init = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Zou, Gote))),
        Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, None),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq(Hiyoko))
    )
    val moved = init.moveKoma(MoveKomaRequest.FromKomadai(Hiyoko, A3, Gote))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Zou, Gote))),
          Masu(B3, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, None),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    )
    assert(moved == expected)
  }

  it should "駒台に駒が無い時は駒を打てない" in {
    val init = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Zou, Gote))),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, None),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq(Hiyoko)),
      goteKomadai = Komadai(Gote, Seq(Hiyoko))
    )
    val moved    = init.moveKoma(MoveKomaRequest.FromKomadai(Zou, B3, Sente))
    val expected = Left(MoveKomaError.KomadaiNiKomagaNai)
    assert(moved == expected)
  }

  it should "駒台から打とうとしたところに駒があると駒を打てない" in {
    val init = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Zou, Gote))),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, None),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq(Hiyoko)),
      goteKomadai = Komadai(Gote, Seq(Hiyoko))
    )
    val moved    = init.moveKoma(MoveKomaRequest.FromKomadai(Hiyoko, A1, Gote))
    val expected = Left(MoveKomaError.UtoutoSitatokoniKomagaAru)
    assert(moved == expected)
  }

  /* 成りに関して */

  it should "ひよこは一番奥の列で成ることができる(先手)" in {
    val board = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, Some(PlayersKoma(Zou, Gote))),
        Masu(C2, Some(PlayersKoma(Hiyoko, Sente))),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )
    val moved = board.moveKoma(MoveKomaRequest.Ugokasu(C2, C1, Sente, true))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
          Masu(B3, None),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Niwatori, Sente))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq(Zou)),
        goteKomadai = Komadai(Gote, Seq.empty)
      )
    )

    assert(moved == expected)
  }

  it should "ひよこは一番手前の列で成ることができる(後手)" in {
    val board = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, None),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, Some(PlayersKoma(Zou, Gote))),
        Masu(C2, Some(PlayersKoma(Hiyoko, Sente))),
        Masu(C3, Some(PlayersKoma(Hiyoko, Gote))),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )
    val moved = board.moveKoma(MoveKomaRequest.Ugokasu(C3, C4, Gote, true))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, None),
          Masu(B3, None),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, Some(PlayersKoma(Hiyoko, Sente))),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Niwatori, Gote)))
        ),
        senteKomadai = Komadai(Sente, Seq.empty),
        goteKomadai = Komadai(Gote, Seq(Kirin))
      )
    )

    assert(moved == expected)
  }

  it should "成れない駒は成ることができない" in {
    val board = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, None),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Lion, Gote))),
        Masu(B2, Some(PlayersKoma(Hiyoko, Gote))),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, Some(PlayersKoma(Zou, Gote))),
        Masu(C2, Some(PlayersKoma(Hiyoko, Sente))),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq.empty),
      goteKomadai = Komadai(Gote, Seq.empty)
    )
    val moved    = board.moveKoma(MoveKomaRequest.Ugokasu(A4, B3, Sente, true))
    val expected = Left(MoveKomaError.NarenaiNoniNaroutoSuru)

    assert(moved == expected)
  }

  it should "ひよこは成れない場所では成れない" in {
    val board    = Board.factory
    val moved    = board.moveKoma(MoveKomaRequest.Ugokasu(B3, B2, Sente, true))
    val expected = Left(MoveKomaError.NarenaiNoniNaroutoSuru)

    assert(moved == expected)
  }

  it should "相手のにわとりを取ったら手駒のひよこにする" in {
    val board = Board(
      masus = Set(
        Masu(A1, Some(PlayersKoma(Kirin, Gote))),
        Masu(A2, Some(PlayersKoma(Lion, Gote))),
        Masu(A3, None),
        Masu(A4, Some(PlayersKoma(Zou, Sente))),
        Masu(B1, Some(PlayersKoma(Niwatori, Sente))),
        Masu(B2, None),
        Masu(B3, None),
        Masu(B4, Some(PlayersKoma(Lion, Sente))),
        Masu(C1, Some(PlayersKoma(Zou, Gote))),
        Masu(C2, None),
        Masu(C3, None),
        Masu(C4, Some(PlayersKoma(Kirin, Sente)))
      ),
      senteKomadai = Komadai(Sente, Seq(Hiyoko)),
      goteKomadai = Komadai(Gote, Seq.empty)
    )
    val moved = board.moveKoma(MoveKomaRequest.Ugokasu(A2, B1, Gote, false))
    val expected = Right(
      Board(
        masus = Set(
          Masu(A1, Some(PlayersKoma(Kirin, Gote))),
          Masu(A2, None),
          Masu(A3, None),
          Masu(A4, Some(PlayersKoma(Zou, Sente))),
          Masu(B1, Some(PlayersKoma(Lion, Gote))),
          Masu(B2, None),
          Masu(B3, None),
          Masu(B4, Some(PlayersKoma(Lion, Sente))),
          Masu(C1, Some(PlayersKoma(Zou, Gote))),
          Masu(C2, None),
          Masu(C3, None),
          Masu(C4, Some(PlayersKoma(Kirin, Sente)))
        ),
        senteKomadai = Komadai(Sente, Seq(Hiyoko)),
        goteKomadai = Komadai(Gote, Seq(Hiyoko))
      )
    )

    moved shouldEqual expected
  }

}
