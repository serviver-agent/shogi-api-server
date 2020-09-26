package web.session

import scala.util.Random

/**
  * RFC7235 token68 文字列
  * https://tools.ietf.org/html/rfc7235
  * token68 = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
  */
sealed abstract case class Token68 private (private val value: String) {
  require(Token68.validate(value))

  def asString: String = value
}

object Token68 {

  private val FirstHalfChars       = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Seq('-', '.', '_', '~', '+', '/')
  private val FirstHalfCharsLength = 68

  /**
    * 指定された長さの RFC7235 token68 文字列をランダムで生成する。
    * 末尾の `=` は任意の個数が認められるが、簡単のため1個とする
    *
    * @param length 末尾の `=` を含んだ文字列の長さ。2未満の場合 IllegalArgumentException
    * @return
    */
  def create(length: Int): Token68 = {
    require(length >= 2)

    val arr   = Array(FirstHalfChars: _*)
    val value = (List.fill(length - 1)(arr(Random.nextInt(FirstHalfCharsLength))) ::: '=' :: Nil).mkString
    new Token68(value) {}
  }

  /**
    * 条件を満たしている文字列の場合 Some
    */
  def fromString(value: String): Option[Token68] = {
    if (validate(value)) Some(new Token68(value) {})
    else None
  }

  def validate(value: String): Boolean = {
    value.splitAt(value.indexWhere(_ == '=')) match {
      case ("", "")        => false
      case ("", first)     => first.forall(FirstHalfChars.contains)
      case (first, second) => first.forall(FirstHalfChars.contains) && second.forall(_ == '=')
    }
  }

}
