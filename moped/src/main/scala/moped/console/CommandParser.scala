package moped.console

import scala.language.experimental.macros

import moped.macros._
import moped.json._
import moped.internal.console.CommandLineParser
import scala.annotation.StaticAnnotation
import moped.annotations.CommandName
import moped.annotations.Hidden
import org.typelevel.paiges.Doc
import moped.annotations.Description
import moped.annotations.DescriptionDoc
import java.io.PrintStream
import moped.annotations.TabComplete

trait CommandParser[A <: BaseCommand] extends JsonCodec[A] {
  type Value = A
  def asClassDefinition: ClassShaper[Value] = this
  def asDecoder: JsonDecoder[Value] = this
  def description: Doc = this.commandLineDescription.getOrElse(Doc.empty)
  def usage: Doc = this.commandLineUsage.getOrElse(Doc.empty)
  def options: Doc = Doc.empty
  def examples: Doc = Doc.intercalate(Doc.line, this.commandLineExamples)
  def isHidden: Boolean = annotations.contains(Hidden())
  def matchesName(name: String): Boolean = allNames.contains(name)
  def helpMessage: Doc = {
    val docs = List[(String, Doc)](
      "USAGE:" -> usage,
      "DESCRIPTION:" -> description,
      "OPTIONS:" -> options,
      "EXAMPLES:" -> examples
    ).collect {
      case (key, doc) if doc.nonEmpty =>
        Doc.text(key) + Doc.line + doc.indent(2)
    }
    val blank = Doc.line + Doc.line
    Doc.intercalate(blank, docs)
  }
  final def helpMessage(out: PrintStream, width: Int): Unit = {
    out.println(helpMessage.renderTrim(width))
  }
  def subcommandName: String =
    annotations
      .collectFirst {
        case CommandName(name) => name
      }
      .getOrElse(throw new NoSuchElementException("@CommandName annotation"))
  def decodeCommand(context: DecodingContext): DecodingResult[BaseCommand] =
    this.decode(context)
  def parseCommand(arguments: List[String]): DecodingResult[BaseCommand] =
    CommandLineParser
      .parseArgs[A](arguments)(this)
      .flatMap(elem => decodeCommand(DecodingContext(elem)))
  def withTabCompletion(
      fn: TabCompletionContext => List[TabCompletionItem]
  ): CommandParser[A] = ???
  def complete(context: TabCompletionContext): List[TabCompletionItem] =
    annotations
      .collectFirst { case TabComplete(fn) => fn.complete(context) }
      .getOrElse(Nil)
}

object CommandParser {
  def derive[A](default: A): CommandParser[A] =
    macro moped.internal.macros.Macros.deriveCommandParserImpl[A]
  def apply[A <: BaseCommand](implicit ev: CommandParser[A]): CommandParser[A] =
    ev
  def fromCodec[A <: BaseCommand](codec: JsonCodec[A]): CommandParser[A] =
    new CodecCommandParser(codec)

}
