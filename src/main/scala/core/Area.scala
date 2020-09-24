package core

/**
  * 盤面の座標は先手番の視点で左上をA1とし、横軸をx（右に正）、縦軸をy（下に正）とする
  * @param x マスのx座標を表す。1~3がA~Cを表す
  * @param y マスのy座標を表す。1~4が1~4を表す　
  */
case class Area(val x: Int, val y: Int) {
  assert(1 <= x && x <= 3 && 1 <= y && y <= 4)

  /**
    * 相対位置分移動するが、盤面からはみ出る場合はNone
    */
  final def move(relativeArea: RelativeArea): Option[Area] = {
    val nextX = x + relativeArea.x
    val nextY = y + relativeArea.y // FIXME: Areaのyは下向きが正だが、RelativeAreaのyは上向きが正である
    if (1 <= nextX && nextX <= 3 && 1 <= nextY && nextY <= 4) Some(Area(nextX, nextY))
    else None
  }

  final def isNareru(player: Player): Boolean = {
    player match {
      case Sente => y == 1
      case Gote  => y == 4
    }
  }
}

object Area {
  val A1: Area = Area(1, 1)
  val A2: Area = Area(1, 2)
  val A3: Area = Area(1, 3)
  val A4: Area = Area(1, 4)
  val B1: Area = Area(2, 1)
  val B2: Area = Area(2, 2)
  val B3: Area = Area(2, 3)
  val B4: Area = Area(2, 4)
  val C1: Area = Area(3, 1)
  val C2: Area = Area(3, 2)
  val C3: Area = Area(3, 3)
  val C4: Area = Area(3, 4)
}

/**
  * 先手番から見た駒が動ける相対位置を表している
  * @param x 右に正
  * @param y 下に正
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
  object Up        extends RelativeArea(0, -1)
  object Right     extends RelativeArea(1, 0)
  object Left      extends RelativeArea(-1, 0)
  object Down      extends RelativeArea(0, 1)
  object RightUp   extends RelativeArea(1, -1)
  object RightDown extends RelativeArea(1, 1)
  object LeftUp    extends RelativeArea(-1, -1)
  object LeftDown  extends RelativeArea(-1, 1)
}
