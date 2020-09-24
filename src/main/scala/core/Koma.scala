package core

import core.RelativeArea._

/**
  * 駒を表す
  *
  */
sealed trait Koma {
  protected def relativeAreaSeenFromSente: Set[RelativeArea]
  final def relativeArea(player: Player): Set[RelativeArea] = player match {
    case Sente => relativeAreaSeenFromSente
    case Gote  => relativeAreaSeenFromSente.map { case RelativeArea(x, y) => RelativeArea(-x, -y) }
  }
  def nari: Option[Koma]

  final def owned(player: Player): PlayersKoma = PlayersKoma(this, player)

}

object Koma {
  case object Lion extends Koma {
    override protected val relativeAreaSeenFromSente: Set[RelativeArea] =
      Set(Up, Right, Down, Left, RightUp, RightDown, LeftUp, LeftDown)
    override def nari: Option[Koma] = None
  }
  case object Kirin extends Koma {
    override protected val relativeAreaSeenFromSente: Set[RelativeArea] = Set(Up, Right, Down, Left)
    override def nari: Option[Koma]                                     = None
  }
  case object Zou extends Koma {
    override protected val relativeAreaSeenFromSente: Set[RelativeArea] = Set(RightUp, RightDown, LeftUp, LeftDown)
    override def nari: Option[Koma]                                     = None
  }
  case object Hiyoko extends Koma {
    override protected val relativeAreaSeenFromSente: Set[RelativeArea] = Set(Up)
    override def nari: Option[Koma]                                     = Some(Niwatori)
  }
  case object Niwatori extends Koma {
    override protected val relativeAreaSeenFromSente: Set[RelativeArea] = Set(Up, Right, Down, Left, RightUp, RightDown)
    override def nari: Option[Koma]                                     = None
  }
}
