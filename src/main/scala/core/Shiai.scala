package core

import core.Board

final case class Shiai(
    shogiban: Board,
    shiaiStatus: Shiai.ShiaiStatus
)

object Shiai {
  sealed trait ShiaiStatus
  object ShiaiStatus {
    object Player1 extends ShiaiStatus
    object Player2 extends ShiaiStatus
    object Finish  extends ShiaiStatus
  }

  def init: Shiai = Shiai(Board.factory, ShiaiStatus.Player1)

}
