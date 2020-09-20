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
  val actualKomas: Seq[Koma] = Seq(
    a1.maybeKoma,
    a2.maybeKoma,
    a3.maybeKoma,
    a4.maybeKoma,
    b1.maybeKoma,
    b2.maybeKoma,
    b3.maybeKoma,
    b4.maybeKoma,
    c1.maybeKoma,
    c2.maybeKoma,
    c3.maybeKoma,
    c4.maybeKoma
  ).flatten ++ senteKomadai.komas ++ goteKomadai.komas

  assert(
    actualKomas.collect { case lion: Lion     => lion }.length == 2 &&
      actualKomas.collect { case kirin: Kirin => kirin }.length == 2 &&
      actualKomas.collect { case zou: Zou     => zou }.length == 2 &&
      (
        (actualKomas.collect { case hiyoko: Hiyoko       => hiyoko }.length == 2 &&
          actualKomas.collect { case niwatori: Niwatori => niwatori }.length == 0) ||
          (
            actualKomas.collect { case hiyoko: Hiyoko       => hiyoko }.length == 0 &&
            actualKomas.collect { case niwatori: Niwatori => niwatori }.length == 2
          ) ||
          (actualKomas.collect { case hiyoko: Hiyoko      => hiyoko }.length == 1 &&
            actualKomas.collect { case niwatori: Niwatori => niwatori }.length == 1)
      )
  )
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
}
