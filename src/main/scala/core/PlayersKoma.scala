package core

/**
  * マスの上にある駒で、どのプレイヤーが所持しているか(どの向きを向いているか)の情報を含む
  *
  * @param koma
  * @param player
  */
case class PlayersKoma(koma: Koma, player: Player)
