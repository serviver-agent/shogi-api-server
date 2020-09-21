package core

/**
  * 駒台を表す
  */
case class Komadai[P <: Player](player: P, komas: Seq[Koma])
