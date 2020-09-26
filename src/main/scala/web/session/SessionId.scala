package web.session

import java.util.UUID

sealed abstract case class SessionId private (private val value: UUID) {
  def asString: String = value.toString
}

object SessionId {

  private[session] def create(): SessionId = new SessionId(UUID.randomUUID()) {}

}
