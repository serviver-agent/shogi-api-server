package web.session

sealed abstract case class Bearer private (private val token68: Token68) {
  def asString: String = s"Bearer ${token68.asString}"
}

object Bearer {

  /**
    * 条件を満たしている文字列の場合 Some
    */
  def fromString(value: String): Option[Bearer] = {
    value.splitAt(7) match {
      case ("Bearer ", tokenStr) => Token68.fromString(tokenStr).map(new Bearer(_) {})
      case _                     => None
    }
  }

  def create(): Bearer = new Bearer(Token68.create(65)) {}

}
