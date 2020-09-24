package core

import core.Area.{A1, A2, A3, A4, B1, B2, B3, B4, C1, C2, C3, C4}
import core.Koma.{Hiyoko, Kirin, Lion, Zou, Niwatori}
import core.Board.{MoveKomaRequest, MoveKomaError}
import core.Game.{GameStatus, GameError}
import org.scalatest.flatspec.AnyFlatSpec

class GameSpec extends AnyFlatSpec {

  it should "試合を開始し、交互に手を指すことができる" in {
    val gameInit     = Game.init
    val senteCommand = Game.MoveKomaRequestBuilder(Sente)
    val goteCommand  = Game.MoveKomaRequestBuilder(Gote)

    val game = gameInit
      .next(senteCommand.ugokasu(B3, B2, false))
      .flatMap(_.next(goteCommand.ugokasu(C1, B2, false)))
      .flatMap(_.next(senteCommand.ugokasu(A4, B3, false)))

    val expected = Right(
      Game(
        board = Board(
          masus = Set(
            Masu(A1, Some(PlayersKoma(Kirin, Gote))),
            Masu(A2, None),
            Masu(A3, None),
            Masu(A4, None),
            Masu(B1, Some(PlayersKoma(Lion, Gote))),
            Masu(B2, Some(PlayersKoma(Zou, Gote))),
            Masu(B3, Some(PlayersKoma(Zou, Sente))),
            Masu(B4, Some(PlayersKoma(Lion, Sente))),
            Masu(C1, None),
            Masu(C2, None),
            Masu(C3, None),
            Masu(C4, Some(PlayersKoma(Kirin, Sente)))
          ),
          senteKomadai = Komadai(Sente, Seq(Hiyoko)),
          goteKomadai = Komadai(Gote, Seq(Hiyoko))
        ),
        status = GameStatus.Processing(nextPlayer = Gote)
      )
    )

    assert(game == expected)
  }

  it should "手番は二回連続で指すことはできない" in {
    val gameInit     = Game.init
    val senteCommand = Game.MoveKomaRequestBuilder(Sente)

    val game = gameInit
      .next(senteCommand.ugokasu(B3, B2, false))
      .flatMap(_.next(senteCommand.ugokasu(A4, B3, false)))

    val expected = Left(GameError.IllegalOrder)

    assert(game == expected)
  }

}
