package mopt

import scala.language.experimental.macros

package object generic {
  def deriveSurface[T]: Surface[T] =
    macro mopt.internal.Macros.deriveSurfaceImpl[T]
  def deriveDecoder[T](default: T): JsonDecoder[T] =
    macro mopt.internal.Macros.deriveJsonDecoderImpl[T]
  def deriveEncoder[T]: JsonEncoder[T] =
    macro mopt.internal.Macros.deriveJsonEncoderImpl[T]
  def deriveCodec[T](default: T): JsonCodec[T] =
    macro mopt.internal.Macros.deriveJsonCodecImpl[T]
}
