package core

sealed trait Location
case class Masu(area: Area, var maybeKoma: Option[Koma]) extends Location
case class Komadai(player: Player, var komas: Seq[Koma]) extends Location

object Location {
  val senteKomadai = Komadai(Sente, Seq.empty)
  val goteKomadai  = Komadai(Gote, Seq.empty)
}

/**
  * マスの座標を表す
  * @param x マスのx座標を表す。1~3がA~Cを表す
  * @param y マスのy座標を表す。1~4が1~4を表す
  *
  */
case class Area(x: Int, y: Int) {
  assert(1 <= x && x <= 3 && 1 <= y && y <= 4)
}

object Area {
  val a1: Area = Area(1, 1)
  val a2       = Area(1, 2)
  val a3       = Area(1, 3)
  val a4       = Area(1, 4)
  val b1       = Area(2, 1)
  val b2       = Area(2, 2)
  val b3       = Area(2, 3)
  val b4       = Area(2, 4)
  val c1       = Area(3, 1)
  val c2       = Area(3, 2)
  val c3       = Area(3, 3)
  val c4       = Area(3, 4)
}


/**
 * 駒が動ける相対位置を表している
 * @param x
 * @param y
 *
 *
 * (x, y)
 *
 * (-1, 1 ) | (0 , 1 ) | (1 , 1 )
 * -----------------------------
 * (-1, 0 ) | 現在位置  | (1 , 0 )
 * -----------------------------
 * (-1, -1) | (0 , -1) | (1 , -1)
 */
private[core] case class RelativeArea(x: Int , y: Int) {
  assert(-1 <= x && x <= 1 && -1 <= y && y <= 1)
}


object RelativeArea {
  val up = RelativeArea(0, 1)
  val right = RelativeArea(1, 0)
  val left = RelativeArea(-1, 0)
  val down = RelativeArea(0, -1)
  val rightUp = RelativeArea(1, 1)
  val rightDown = RelativeArea(1, -1)
  val leftUp = RelativeArea(-1, 1)
  val leftDown = RelativeArea(-1, -1)
}
