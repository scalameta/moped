package moped

import scala.language.experimental.macros
import moped.json._
import java.lang.instrument.ClassDefinition

package object macros {
  def deriveShaper[T]: ClassShaper[T] =
    macro moped.internal.macros.Macros.deriveShaperImpl[T]
  def deriveDecoder[T](default: T): JsonDecoder[T] =
    macro moped.internal.macros.Macros.deriveJsonDecoderImpl[T]
  def deriveEncoder[T]: JsonEncoder[T] =
    macro moped.internal.macros.Macros.deriveJsonEncoderImpl[T]
  def deriveCodec[T](default: T): JsonCodec[T] =
    macro moped.internal.macros.Macros.deriveJsonCodecImpl[T]
}
