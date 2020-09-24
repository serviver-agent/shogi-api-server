package core

import Board.{MoveKomaRequest, MoveKomaError}
import Game._
import core.Game.GameStatus.Finished
import core.Game.GameStatus.Processing

/**
  * 対局のモデル。ボードの情報に加えて、手番の管理・終了の判定を行う。
  *
  */
case class Game(board: Board, status: GameStatus) {

  def next(request: MoveKomaRequest): Either[GameError, Game] = {
    status match {
      case Finished(_) => Left(GameError.GameIsOver)
      case Processing(nextPlayer) => {
        for {
          _         <- Either.cond(request.player == nextPlayer, (), GameError.IllegalOrder)
          nextBoard <- board.next(request).left.map(e => GameError.IllegalMeans(e))
        } yield {
          nextBoard.winnerOpt match {
            case Some(winner) => Game(nextBoard, GameStatus.Finished(winner))
            case None         => Game(nextBoard, GameStatus.Processing(nextPlayer.next))
          }
        }
      }
    }
  }

}

object Game {

  def init: Game = Game(Board.factory, GameStatus.Processing(Sente))

  class MoveKomaRequestBuilder(player: Player) {
    def ugokasu(from: Area, to: Area, nari: Boolean): MoveKomaRequest.Ugokasu =
      MoveKomaRequest.Ugokasu(from, to, player, nari)
    def fromKomadai(koma: Koma, to: Area): MoveKomaRequest.FromKomadai =
      MoveKomaRequest.FromKomadai(koma, to, player)
  }

  sealed trait GameStatus
  object GameStatus {
    case class Processing(nextPlayer: Player) extends GameStatus
    case class Finished(winner: Player)       extends GameStatus
  }

  sealed trait GameError
  object GameError {
    case object IllegalOrder                      extends GameError
    case object GameIsOver                        extends GameError
    case class IllegalMeans(error: MoveKomaError) extends GameError
  }

}
