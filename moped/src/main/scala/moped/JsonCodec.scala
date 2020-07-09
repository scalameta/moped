package moped

import scala.language.higherKinds

trait JsonCodec[A] extends JsonDecoder[A] with JsonEncoder[A] { self =>
  def bimap[B](in: B => A, out: A => B): JsonCodec[B] =
    new JsonCodec[B] {
      override def encode(value: B): JsonElement =
        self.encode(in(value))
      override def decode(conf: DecodingContext): DecodingResult[B] =
        self.decode(conf).map(out)
    }
}

object JsonCodec {
  def apply[A](implicit ev: JsonCodec[A]): JsonCodec[A] = ev
  implicit def encoderDecoderJsonCodec[A](implicit
      e: JsonEncoder[A],
      d: JsonDecoder[A]
  ): JsonCodec[A] =
    new JsonCodec[A] {
      override def encode(value: A): JsonElement =
        e.encode(value)
      override def decode(conf: DecodingContext): DecodingResult[A] =
        d.decode(conf)
    }

  val IntCodec: JsonCodec[Int] = JsonCodec[Int]
  val StringCodec: JsonCodec[String] = JsonCodec[String]
  val BooleanCodec: JsonCodec[Boolean] = JsonCodec[Boolean]
}
