package moped.console

import moped.json.DecodingContext
import moped.json.DecodingResult
import moped.json.JsonCodec
import moped.json.JsonElement
import moped.macros.ClassShape
import org.typelevel.paiges.Doc
import moped.internal.console.HelpMessage

class CodecCommandParser[A <: BaseCommand](
    val codec: JsonCodec[A],
    val default: A
) extends CommandParser[A] {
  override def options: Doc =
    HelpMessage.generate(default)(codec, codec)
  def decode(context: DecodingContext): DecodingResult[A] =
    codec.decode(context)
  def encode(value: A): JsonElement =
    codec.encode(value)
  def shape: ClassShape =
    codec.shape
}
