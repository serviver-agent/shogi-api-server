package core

import core.RelativeArea._

/**
  * 駒を表す
  *
  */
sealed trait Koma {
  def hasPlayer: Player
  def action: Set[RelativeArea]
}

object Koma {
  case class Lion(hasPlayer: Player) extends Koma {
    override val action: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown, LeftUp, LeftDown)
  }
  case class Kirin(hasPlayer: Player) extends Koma {
    override val action: Set[RelativeArea] = Set(Up, Right, Down, Left)
  }
  case class Zou(hasPlayer: Player) extends Koma {
    override val action: Set[RelativeArea] = Set(RightUp, RightDown, LeftUp, LeftDown)
  }
  case class Hiyoko(hasPlayer: Player) extends Koma {
    override val action: Set[RelativeArea] = Set(Up)
  }
  case class Niwatori(hasPlayer: Player) extends Koma {
    override val action: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown)
  }
}
