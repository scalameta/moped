package moped.json

import scala.language.higherKinds
import scala.language.experimental.macros
import moped.generic.ClassShaper
import moped.generic.ParameterShape
import scala.annotation.StaticAnnotation
import moped.generic.ClassShape

trait JsonCodec[A]
    extends JsonDecoder[A]
    with JsonEncoder[A]
    with ClassShaper[A] { self =>

  def bimap[B](in: B => A, out: A => B): JsonCodec[B] =
    new JsonCodec[B] {
      override def shape: ClassShape =
        self.shape
      override def encode(value: B): JsonElement =
        self.encode(in(value))
      override def decode(conf: DecodingContext): DecodingResult[B] =
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
      override def shape: ClassShape =
        c.shape
      override def encode(value: A): JsonElement =
        e.encode(value)
      override def decode(conf: DecodingContext): DecodingResult[A] =
        d.decode(conf)
    }

}
