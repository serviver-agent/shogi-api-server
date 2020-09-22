package core

import core.RelativeArea._

/**
  * 駒を表す
  *
  */
sealed trait Koma {
  def player: Player
  def relativeArea: Set[RelativeArea]
  def owned(nextPlayer: Player): Koma
}

object Koma {
  case class Lion(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown, LeftUp, LeftDown)
    override def owned(nextPlayer: Player): Koma = copy(player = nextPlayer)
  }
  case class Kirin(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left)
    override def owned(nextPlayer: Player): Koma = copy(player = nextPlayer)
  }
  case class Zou(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(RightUp, RightDown, LeftUp, LeftDown)
    override def owned(nextPlayer: Player): Koma = copy(player = nextPlayer)
  }
  case class Hiyoko(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up)
    override def owned(nextPlayer: Player): Koma = copy(player = nextPlayer)
  }
  case class Niwatori(player: Player) extends Koma {
    override val relativeArea: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown)
    override def owned(nextPlayer: Player): Koma = copy(player = nextPlayer)
  }
}
