package core

case class Shogiban(
    lion1: KomaStatus,
    lion2: KomaStatus,
    kirin1: KomaStatus,
    kirin2: KomaStatus,
    zou1: KomaStatus,
    zou2: KomaStatus,
    hiyoko1: KomaStatus,
    hiyoko2: KomaStatus
)

object Shogiban {

  def init: Shogiban = {
    import core.Location.OnShogiban
    import core.Location.Yoko._
    import core.Location.Tate._
    Shogiban(
      lion1 = KomaStatus(Koma.Lion, OnShogiban(Y_B, T_4)),
      kirin1 = KomaStatus(Koma.Kirin, OnShogiban(Y_C, T_4)),
      zou1 = KomaStatus(Koma.Zou, OnShogiban(Y_A, T_4)),
      hiyoko1 = KomaStatus(Koma.Hiyoko, OnShogiban(Y_B, T_3)),
      lion2 = KomaStatus(Koma.Lion, OnShogiban(Y_B, T_1)),
      kirin2 = KomaStatus(Koma.Kirin, OnShogiban(Y_A, T_1)),
      zou2 = KomaStatus(Koma.Zou, OnShogiban(Y_C, T_1)),
      hiyoko2 = KomaStatus(Koma.Hiyoko, OnShogiban(Y_B, T_2))
    )
  }

}
