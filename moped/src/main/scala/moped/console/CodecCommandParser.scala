package moped.console

import moped.json.JsonCodec
import moped.json.{DecodingContext, DecodingResult}
import moped.json.JsonElement
import moped.generic.ParameterDefinition
import scala.annotation.StaticAnnotation

class CodecCommandParser[A <: BaseCommand](val codec: JsonCodec[A])
    extends CommandParser[A] {
  def decode(context: DecodingContext): DecodingResult[A] =
    codec.decode(context)
  def encode(value: A): JsonElement =
    codec.encode(value)
  def fields: List[List[ParameterDefinition]] =
    codec.fields
  def annotations: List[StaticAnnotation] =
    codec.annotations
}
