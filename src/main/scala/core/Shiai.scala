package core

import core.Board

final case class Shiai(
    board: Board,
    status: Shiai.Status
)

object Shiai {
  sealed trait Status
  object Status {
    object Player1 extends Status
    object Player2 extends Status
    object Finish  extends Status
  }

  def init: Shiai = Shiai(Board.factory, Status.Player1)

}
