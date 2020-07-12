package moped.console

import moped.json.JsonCodec
import moped.json.{DecodingContext, DecodingResult}
import moped.json.JsonElement
import scala.annotation.StaticAnnotation
import moped.macros.ClassShape

class CodecCommandParser[A <: BaseCommand](val codec: JsonCodec[A])
    extends CommandParser[A] {
  def decode(context: DecodingContext): DecodingResult[A] =
    codec.decode(context)
  def encode(value: A): JsonElement =
    codec.encode(value)
  def shape: ClassShape =
    codec.shape
}
