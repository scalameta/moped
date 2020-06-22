package moped

import moped.json._

package object macros {
  import scala.language.experimental.macros
  def deriveShaper[T]: ClassShaper[T] =
    macro moped.internal.macros.Macros.deriveShaperImpl[T]
  def deriveDecoder[T](default: T): JsonDecoder[T] =
    macro moped.internal.macros.Macros.deriveJsonDecoderImpl[T]
  def deriveEncoder[T]: JsonEncoder[T] =
    macro moped.internal.macros.Macros.deriveJsonEncoderImpl[T]
  def deriveCodec[T](default: T): JsonCodec[T] =
    macro moped.internal.macros.Macros.deriveJsonCodecImpl[T]
}
