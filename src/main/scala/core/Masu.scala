package core

/**
  *  しょうぎばんのマスを表す
  * @param area マスの座標を表す
  * @param upon マスに乗っている駒、または何も乗っていないことを表す
  */
case class Masu(area: Area, upon: Option[PlayersKoma])

object Masu {

  def replaceMasu(masus: Set[Masu], area: Area, upon: Option[PlayersKoma]): Set[Masu] = {
    masus.filterNot(_.area == area) + Masu(area, upon)
  }

}
