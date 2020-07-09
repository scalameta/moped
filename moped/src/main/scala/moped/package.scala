package moped

import scala.language.experimental.macros

package object generic {
  def deriveSurface[T]: Surface[T] =
    macro moped.internal.Macros.deriveSurfaceImpl[T]
  def deriveDecoder[T](default: T): JsonDecoder[T] =
    macro moped.internal.Macros.deriveJsonDecoderImpl[T]
  def deriveEncoder[T]: JsonEncoder[T] =
    macro moped.internal.Macros.deriveJsonEncoderImpl[T]
  def deriveCodec[T](default: T): JsonCodec[T] =
    macro moped.internal.Macros.deriveJsonCodecImpl[T]
}
