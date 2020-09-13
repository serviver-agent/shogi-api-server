package core

import core.RelativeArea.{down, left, leftDown, leftUp, right, rightDown, rightUp, up}

sealed trait Koma {
  def hasPlayer: Player
  def action: Seq[RelativeArea]
}

object Koma {
  case class Lion(hasPlayer: Player) extends Koma {
    override val action: Seq[RelativeArea] = Seq(up, right, down, left, rightUp, rightDown, leftUp, leftDown)
  }
  case class Kirin(hasPlayer: Player) extends Koma {
    override val action: Seq[RelativeArea] = Seq(up, right, down, left)
  }
  case class Zou(hasPlayer: Player) extends Koma {
    override val action: Seq[RelativeArea] = Seq(rightUp, rightDown, leftUp, leftDown)
  }
  case class Hiyoko(hasPlayer: Player) extends Koma {
    override val action: Seq[RelativeArea] = Seq(up)
  }

  case class Niwatori(hasPlayer: Player) extends Koma {
    override val action: Seq[RelativeArea] = Seq(up, right, down, left, rightUp, rightDown)
  }
}
