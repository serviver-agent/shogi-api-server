package core

import core.Area._
import core.Koma.{Hiyoko, Kirin, Lion, Niwatori, Zou}
import Board._

/**
  * どうぶつしょうぎの盤面を表す
  *
  */
case class Board(
    masus: Set[Masu],
    senteKomadai: Komadai[Sente.type],
    goteKomadai: Komadai[Gote.type]
) {

  validateKomaCount(this)
  validateDuplicateMasu(this)

  // validateDuplicateMasu により確実に取得できる
  private def getMasu(area: Area): Masu = masus.find(_.area == area).get

  def moveKoma(from: Area, to: Area, player: Player): Either[MoveKomaError, Board] = {
    val fromMasu = getMasu(from)
    val toMasu   = getMasu(to)
    for {
      fromKoma <- fromMasu.maybeKoma.toRight(MoveKomaError.FromKomaNotFound)
      _        <- Either.cond(fromKoma.player == player, (), MoveKomaError.FromKomaIsNotOwnedByThatPlayer)
      toKomaOpt = toMasu.maybeKoma
      _ <- Either.cond(
        toKomaOpt.map(_.player) != Some(player),
        (),
        MoveKomaError.IdousakiniJibunnoKomagaAru
      )
      canMoveAreas = fromKoma.relativeArea.map(_.seenFrom(player)).map(from.move).flatten
      _ <- Either.cond(canMoveAreas.contains(to), (), MoveKomaError.KomahaSonobashoniIdouDekinai)
    } yield {
      val (nextSenteKomadai, nextGoteKomadai) = (toKomaOpt, player) match {
        case (None, _)             => (senteKomadai, goteKomadai)
        case (Some(toKoma), Sente) => (senteKomadai.add(toKoma), goteKomadai)
        case (Some(toKoma), Gote)  => (senteKomadai, goteKomadai.add(toKoma))
      }
      val nextMasus = Masu.replaceMasu(Masu.replaceMasu(masus, from, None), to, Some(fromKoma.owned(player)))
      Board(nextMasus, nextSenteKomadai, nextGoteKomadai)
    }
  }

}
object Board {

  def factory: Board = Board(
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

  private def validateKomaCount(board: Board): Unit = {
    val komas: Seq[Koma] =
      board.masus.flatMap(_.maybeKoma).toSeq ++ board.senteKomadai.komas ++ board.goteKomadai.komas

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

  private def validateDuplicateMasu(board: Board): Unit = {
    assert(board.masus.size == 12)
    assert(board.masus.toList.map(_.area).distinct.length == 12)
  }

  sealed trait MoveKomaError
  object MoveKomaError {
    object FromKomaNotFound               extends MoveKomaError
    object FromKomaIsNotOwnedByThatPlayer extends MoveKomaError
    object IdousakiniJibunnoKomagaAru     extends MoveKomaError
    object KomahaSonobashoniIdouDekinai   extends MoveKomaError
  }

}
