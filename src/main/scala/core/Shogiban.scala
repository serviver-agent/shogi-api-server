package core

case class Shogiban(
    lion1: KomaStatus[Koma.Lion.type],
    lion2: KomaStatus[Koma.Lion.type],
    kirin1: KomaStatus[Koma.Kirin.type],
    kirin2: KomaStatus[Koma.Kirin.type],
    zou1: KomaStatus[Koma.Zou.type],
    zou2: KomaStatus[Koma.Zou.type],
    hiyoko1: KomaStatus[Koma.Hiyoko.type],
    hiyoko2: KomaStatus[Koma.Hiyoko.type]
)

object Shogiban {

  def init: Shogiban = {
    import core.Location.Yoko._
    import core.Location.Tate._
    Shogiban(
      lion1 = KomaStatus(Koma.Lion, Location(Y_B, T_4)),
      kirin1 = KomaStatus(Koma.Kirin, Location(Y_C, T_4)),
      zou1 = KomaStatus(Koma.Zou, Location(Y_A, T_4)),
      hiyoko1 = KomaStatus(Koma.Hiyoko, Location(Y_B, T_3)),
      lion2 = KomaStatus(Koma.Lion, Location(Y_B, T_1)),
      kirin2 = KomaStatus(Koma.Kirin, Location(Y_A, T_1)),
      zou2 = KomaStatus(Koma.Zou, Location(Y_C, T_1)),
      hiyoko2 = KomaStatus(Koma.Hiyoko, Location(Y_B, T_2))
    )
  }

}
