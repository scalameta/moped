package moped.json

import moped.console.Environment

final class DecodingContext private (
    val json: JsonElement,
    val cursor: Cursor,
    val environment: Environment
)

object DecodingContext {
  def apply(json: JsonElement): DecodingContext = {
    DecodingContext(json, NoCursor())
  }
  def apply(json: JsonElement, cursor: Cursor): DecodingContext = {
    new DecodingContext(
      json,
      cursor,
      Environment.default
    )
  }
}
