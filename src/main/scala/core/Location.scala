package core

sealed trait Location
case class Masu[A <: Area](area: A, maybeKoma: Option[Koma]) extends Location {
  val x = area.x
  val y = area.y
}
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
sealed abstract class Area(val x: Int, val y: Int) {
  assert(1 <= x && x <= 3 && 1 <= y && y <= 4)
}

object Area {
  case object A1 extends Area(1, 1)

  case object A2 extends Area(2, 1)

  case object A3 extends Area(3, 1)

  case object A4 extends Area(4, 1)

  case object B1 extends Area(1, 2)

  case object B2 extends Area(2, 2)

  case object B3 extends Area(3, 2)

  case object B4 extends Area(4, 2)

  case object C1 extends Area(1, 3)

  case object C2 extends Area(2, 3)

  case object C3 extends Area(3, 3)

  case object C4 extends Area(4, 3)
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
private[core] case class RelativeArea(x: Int, y: Int) {
  assert(-1 <= x && x <= 1 && -1 <= y && y <= 1)
}

object RelativeArea {
  val up        = RelativeArea(0, 1)
  val right     = RelativeArea(1, 0)
  val left      = RelativeArea(-1, 0)
  val down      = RelativeArea(0, -1)
  val rightUp   = RelativeArea(1, 1)
  val rightDown = RelativeArea(1, -1)
  val leftUp    = RelativeArea(-1, 1)
  val leftDown  = RelativeArea(-1, -1)
}
