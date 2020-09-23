package core

/**
  *  しょうぎばんのマスを表す
  * @param area マスの座標を表す
  * @param maybeKoma マスに乗っている駒、または何も乗っていないことを表す
  */
case class Masu(area: Area, maybeKoma: Option[Koma]) {
  val x = area.x
  val y = area.y
}

object Masu {

  def replaceMasu(masus: Set[Masu], area: Area, maybeKoma: Option[Koma]): Set[Masu] = {
    masus.filterNot(_.area == area) + Masu(area, maybeKoma)
  }

}
