package web.adapter

trait Decoder[A] {
  def decode(in: String): Option[A]
}

trait Encoder[B] {
  def encode(out: B): String
}
