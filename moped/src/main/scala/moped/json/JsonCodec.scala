package moped.json

import moped.macros.ClassShape
import moped.macros.ClassShaper

trait JsonCodec[A]
    extends JsonDecoder[A]
    with JsonEncoder[A]
    with ClassShaper[A] {
  self =>

  def bimap[B](in: B => A, out: A => B): JsonCodec[B] =
    new JsonCodec[B] {
      override def shape: ClassShape = self.shape
      override def encode(value: B): JsonElement = self.encode(in(value))
      override def decode(conf: DecodingContext): Result[B] =
        self.decode(conf).map(out)
    }
}

object JsonCodec {
  def apply[A](implicit ev: JsonCodec[A]): JsonCodec[A] = ev
  implicit def encoderDecoderJsonCodec[A](implicit
      c: ClassShaper[A],
      e: JsonEncoder[A],
      d: JsonDecoder[A]
  ): JsonCodec[A] =
    new JsonCodec[A] {
      override def shape: ClassShape = c.shape
      override def encode(value: A): JsonElement = e.encode(value)
      override def decode(conf: DecodingContext): Result[A] = d.decode(conf)
    }

}
