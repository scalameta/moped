package moped.json

import scala.language.higherKinds
import scala.language.experimental.macros
import moped.generic.ClassDefinition
import moped.generic.ParameterDefinition
import scala.annotation.StaticAnnotation

trait JsonCodec[A]
    extends JsonDecoder[A]
    with JsonEncoder[A]
    with ClassDefinition[A] { self =>

  def bimap[B](in: B => A, out: A => B): JsonCodec[B] =
    new JsonCodec[B] {
      override def fields: List[List[ParameterDefinition]] =
        self.fields
      override def annotations: List[StaticAnnotation] =
        self.annotations
      override def encode(value: B): JsonElement =
        self.encode(in(value))
      override def decode(conf: DecodingContext): DecodingResult[B] =
        self.decode(conf).map(out)
    }
}

object JsonCodec {
  def apply[A](implicit ev: JsonCodec[A]): JsonCodec[A] = ev
  implicit def encoderDecoderJsonCodec[A](implicit
      c: ClassDefinition[A],
      e: JsonEncoder[A],
      d: JsonDecoder[A]
  ): JsonCodec[A] =
    new JsonCodec[A] {
      override def fields: List[List[ParameterDefinition]] =
        c.fields
      override def annotations: List[StaticAnnotation] =
        c.annotations
      override def encode(value: A): JsonElement =
        e.encode(value)
      override def decode(conf: DecodingContext): DecodingResult[A] =
        d.decode(conf)
    }

}
