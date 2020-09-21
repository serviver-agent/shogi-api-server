package core

import core.Area._
import core.Koma.{Hiyoko, Kirin, Lion, Niwatori, Zou}

/**
  * どうぶつしょうぎの盤面を表す
  *
  */
case class Board(
    a1: Masu[A1.type],
    a2: Masu[A2.type],
    a3: Masu[A3.type],
    a4: Masu[A4.type],
    b1: Masu[B1.type],
    b2: Masu[B2.type],
    b3: Masu[B3.type],
    b4: Masu[B4.type],
    c1: Masu[C1.type],
    c2: Masu[C2.type],
    c3: Masu[C3.type],
    c4: Masu[C4.type],
    senteKomadai: Komadai[Sente.type],
    goteKomadai: Komadai[Gote.type]
) {

  Board.validateKomaCount(this)

}
object Board {
  def factory: Board = Board(
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

  private def validateKomaCount(board: Board): Unit = {
    val komas: Seq[Koma] = Seq(
      board.a1.maybeKoma,
      board.a2.maybeKoma,
      board.a3.maybeKoma,
      board.a4.maybeKoma,
      board.b1.maybeKoma,
      board.b2.maybeKoma,
      board.b3.maybeKoma,
      board.b4.maybeKoma,
      board.c1.maybeKoma,
      board.c2.maybeKoma,
      board.c3.maybeKoma,
      board.c4.maybeKoma
    ).flatten ++ board.senteKomadai.komas ++ board.goteKomadai.komas

    val lionCount     = komas.collect { case lion: Lion         => lion }.length
    val kirinCount    = komas.collect { case kirin: Kirin       => kirin }.length
    val zouCount      = komas.collect { case zou: Zou           => zou }.length
    val hiyokoCount   = komas.collect { case hiyoko: Hiyoko     => hiyoko }.length
    val niwatoriCount = komas.collect { case niwatori: Niwatori => niwatori }.length

    assert(lionCount == 2)
    assert(kirinCount == 2)
    assert(zouCount == 2)
    assert(hiyokoCount + niwatoriCount == 2)
  }
}
