package moped

import moped.generic.Surface
import moped.internal.CommandLineParser
import moped.generic.Structure

abstract class CommandParser[A <: BaseCommand] {
  def decoder: JsonDecoder[A]
  def encoder: JsonEncoder[A]
  def settings: Structure[A]
  def parseCommand(arguments: List[String]): DecodingResult[BaseCommand] =
    CommandLineParser
      .parseArgs[A](arguments)(settings)
      .flatMap(decoder.decode(_))
}

object CommandParser {
  def apply[A <: BaseCommand](implicit
      a: JsonDecoder[A],
      b: JsonEncoder[A],
      c: Structure[A]
  ): CommandParser[A] =
    new CommandParser[A] {
      def decoder: JsonDecoder[A] = a
      def encoder: JsonEncoder[A] = b
      def settings: Structure[A] = c
    }
}
