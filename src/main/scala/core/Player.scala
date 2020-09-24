package core

sealed trait Player {
  def next: Player
}
case object Sente extends Player {
  override def next: Player = Gote
}
case object Gote extends Player {
  override def next: Player = Sente
}
