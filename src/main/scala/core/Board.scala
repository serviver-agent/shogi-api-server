package core

import core.Area._
import core.Koma.{Hiyoko, Kirin, Lion, Niwatori, Zou}
import Board._
import core.Board.MoveKomaRequest.Ugokasu
import core.Board.MoveKomaRequest.FromKomadai

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

  private def getKomadai(player: Player): Komadai[_] = player match {
    case Sente => senteKomadai
    case Gote  => goteKomadai
  }

  def moveKoma(request: MoveKomaRequest): Either[MoveKomaError, Board] = {
    request match {
      case Ugokasu(from, to, player, nari) => ugokasu(from, to, player, nari)
      case FromKomadai(koma, to, player)   => fromKomadai(koma, to, player)
    }
  }

  private def ugokasu(from: Area, to: Area, player: Player, nari: Boolean): Either[MoveKomaError, Board] = {
    val fromMasu = getMasu(from)
    val toMasu   = getMasu(to)
    for {
      fromMasuUpon <- fromMasu.upon.toRight(MoveKomaError.FromKomaNotFound)
      _            <- Either.cond(fromMasuUpon.player == player, (), MoveKomaError.FromKomaIsNotOwnedByThatPlayer)
      _ <- Either.cond(
        toMasu.upon.map(_.player) != Some(player),
        (),
        MoveKomaError.IdousakiniJibunnoKomagaAru
      )
      canMoveAreas = fromMasuUpon.koma.relativeArea(player).map(from.move).flatten
      _ <- Either.cond(canMoveAreas.contains(to), (), MoveKomaError.KomahaSonobashoniIdouDekinai)
      nextKoma <- (nari, to.isNareru(player), fromMasuUpon.koma.nari) match {
        case (true, true, Some(nattaKoma)) => Right(nattaKoma)
        case (true, _, _)                  => Left(MoveKomaError.NarenaiNoniNaroutoSuru)
        case _                             => Right(fromMasuUpon.koma)
      }
    } yield {
      val (nextSenteKomadai, nextGoteKomadai) = (toMasu.upon.map(_.koma), player) match {
        case (None, _)             => (senteKomadai, goteKomadai)
        case (Some(toKoma), Sente) => (senteKomadai.add(toKoma), goteKomadai)
        case (Some(toKoma), Gote)  => (senteKomadai, goteKomadai.add(toKoma))
      }
      val nextMasus = Masu.replaceMasu(Masu.replaceMasu(masus, from, None), to, Some(nextKoma.owned(player)))
      Board(nextMasus, nextSenteKomadai, nextGoteKomadai)
    }
  }

  private def fromKomadai(koma: Koma, to: Area, player: Player): Either[MoveKomaError, Board] = {
    for {
      _ <- Either.cond(getKomadai(player).exists(koma), (), MoveKomaError.KomadaiNiKomagaNai)
      _ <- Either.cond(getMasu(to).upon.isEmpty, (), MoveKomaError.UtoutoSitatokoniKomagaAru)
    } yield {
      val (nextSenteKomadai, nextGoteKomadai) = player match {
        case Sente => (senteKomadai.delete(koma), goteKomadai)
        case Gote  => (senteKomadai, goteKomadai.delete(koma))
      }
      val nextMasus = Masu.replaceMasu(masus, to, Some(koma.owned(player)))
      Board(nextMasus, nextSenteKomadai, nextGoteKomadai)
    }
  }

}

object Board {

  def factory: Board = Board(
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

  private def validateKomaCount(board: Board): Unit = {
    val komas: Seq[Koma] =
      board.masus.toSeq.flatMap(_.upon).map(_.koma) ++ board.senteKomadai.komas ++ board.goteKomadai.komas

    assert(komas.count(_ == Lion) == 2)
    assert(komas.count(_ == Kirin) == 2)
    assert(komas.count(_ == Zou) == 2)
    assert(komas.count(_ == Hiyoko) + komas.count(_ == Niwatori) == 2)
  }

  private def validateDuplicateMasu(board: Board): Unit = {
    assert(board.masus.size == 12)
    assert(board.masus.toList.map(_.area).distinct.length == 12)
  }

  sealed trait MoveKomaRequest
  object MoveKomaRequest {
    case class Ugokasu(from: Area, to: Area, player: Player, nari: Boolean) extends MoveKomaRequest
    case class FromKomadai(koma: Koma, to: Area, player: Player)            extends MoveKomaRequest
  }

  sealed trait MoveKomaError
  object MoveKomaError {
    case object FromKomaNotFound               extends MoveKomaError
    case object FromKomaIsNotOwnedByThatPlayer extends MoveKomaError
    case object IdousakiniJibunnoKomagaAru     extends MoveKomaError
    case object KomahaSonobashoniIdouDekinai   extends MoveKomaError
    case object KomadaiNiKomagaNai             extends MoveKomaError
    case object UtoutoSitatokoniKomagaAru      extends MoveKomaError
    case object NarenaiNoniNaroutoSuru         extends MoveKomaError
  }

}
