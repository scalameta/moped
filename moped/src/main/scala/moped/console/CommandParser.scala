package moped.console

import moped.generic._
import moped.json._
import moped.internal.console.CommandLineParser

abstract class CommandParser[A <: BaseCommand] {
  def decoder: JsonDecoder[A]
  def encoder: JsonEncoder[A]
  def settings: ClassDefinition[A]
  def parseCommand(arguments: List[String]): DecodingResult[BaseCommand] =
    CommandLineParser
      .parseArgs[A](arguments)(settings)
      .flatMap(decoder.decode(_))
}

object CommandParser {
  def apply[A <: BaseCommand](implicit
      a: JsonDecoder[A],
      b: JsonEncoder[A],
      c: ClassDefinition[A]
  ): CommandParser[A] =
    new CommandParser[A] {
      def decoder: JsonDecoder[A] = a
      def encoder: JsonEncoder[A] = b
      def settings: ClassDefinition[A] = c
    }
}
