package web.adapter

trait Apis {
  def sessionsApi: SessionsApi
  def roomApi: RoomApi
  def shogiApp: ShogiMock
}

class ApisImpl(
    override val sessionsApi: SessionsApi,
    override val roomApi: RoomApi,
    override val shogiApp: ShogiMock
) extends Apis
