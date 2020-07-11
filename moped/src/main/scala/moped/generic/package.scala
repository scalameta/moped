package moped

import scala.language.experimental.macros
import moped.json._

package object generic {
  def deriveSurface[T]: Surface[T] =
    macro moped.internal.generic.Macros.deriveSurfaceImpl[T]
  def deriveDecoder[T](default: T): JsonDecoder[T] =
    macro moped.internal.generic.Macros.deriveJsonDecoderImpl[T]
  def deriveEncoder[T]: JsonEncoder[T] =
    macro moped.internal.generic.Macros.deriveJsonEncoderImpl[T]
  def deriveCodec[T](default: T): JsonCodec[T] =
    macro moped.internal.generic.Macros.deriveJsonCodecImpl[T]
}
