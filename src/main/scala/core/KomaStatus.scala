package core

final case class KomaStatus[K <: Koma](koma: K, location: Location)
