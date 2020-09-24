package core

/**
  * 駒台を表す
  */
case class Komadai[P <: Player](player: P, komas: Seq[Koma]) {
  def add(koma: Koma): Komadai[P] = copy(komas = komas :+ koma)
  def delete(koma: Koma): Komadai[P] = {
    val index = komas.indexWhere(_.sameKoma(koma))
    if (index == -1) throw new IllegalArgumentException("駒台にそんな駒はない")
    val nextkomas = komas.take(index) ++ komas.drop(index + 1)
    copy(komas = nextkomas)
  }
  def exists(koma: Koma): Boolean = komas.exists(_.sameKoma(koma))
}
