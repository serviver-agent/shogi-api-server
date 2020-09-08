package core

import Location.{Yoko, Tate}

final case class Location(yoko: Yoko, tate: Tate)

object Location {
  sealed abstract class Yoko(n: Int) {
    assert(0 <= n && n <= 2)
  }
  object Yoko {
    object Y_A extends Yoko(0)
    object Y_B extends Yoko(1)
    object Y_C extends Yoko(2)
  }

  sealed abstract class Tate(n: Int) {
    assert(0 <= n && n <= 3)
  }
  object Tate {
    object T_1 extends Tate(0)
    object T_2 extends Tate(1)
    object T_3 extends Tate(2)
    object T_4 extends Tate(4)
  }
}
