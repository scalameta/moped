package moped.parsers

import moped.json.JsonElement
import moped.json.Result
import moped.reporters.Input

trait ConfigurationParser {
  def supportedFileExtensions: List[String]
  def parse(input: Input): Result[JsonElement]
}
