package mopt

final class DecodingContext private (
    val json: JsonElement,
    val cursor: Cursor
)

object DecodingContext {
  def apply(json: JsonElement): DecodingContext = {
    DecodingContext(json, NoCursor())
  }
  def apply(json: JsonElement, cursor: Cursor): DecodingContext = {
    new DecodingContext(
      json,
      cursor
    )
  }
}
