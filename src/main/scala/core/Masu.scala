package core

/**
  *  しょうぎばんのマスを表す
  * @param area マスの座標を表す
  * @param maybeKoma マスに乗っている駒、または何も乗っていないことを表す
  */
case class Masu[A <: Area](area: A, maybeKoma: Option[Koma]) {
  val x = area.x
  val y = area.y
}