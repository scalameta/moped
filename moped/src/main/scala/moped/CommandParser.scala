package moped

import moped.generic.Surface
import moped.internal.CommandLineParser
import moped.generic.Settings

abstract class CommandParser[A <: BaseCommand] {
  def decoder: JsonDecoder[A]
  def encoder: JsonEncoder[A]
  def settings: Settings[A]
  def parseCommand(arguments: List[String]): DecodingResult[BaseCommand] =
    CommandLineParser
      .parseArgs[A](arguments)(settings)
      .flatMap(decoder.decode(_))
}
