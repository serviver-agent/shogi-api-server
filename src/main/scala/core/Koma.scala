package core

import core.RelativeArea._

/**
  * 駒を表す
  *
  */
sealed trait Koma {
  def player: Player
  def relativeArea: Set[RelativeArea]
}

object Koma {
  case class Lion(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown, LeftUp, LeftDown)
  }
  case class Kirin(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left)
  }
  case class Zou(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(RightUp, RightDown, LeftUp, LeftDown)
  }
  case class Hiyoko(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up)
  }
  case class Niwatori(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown)
  }
}
