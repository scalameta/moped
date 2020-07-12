package moped.console

import org.typelevel.paiges.Doc
import moped.reporters.Terminals
import moped.internal.reporters.Levenshtein
import moped.generic.ClassShaper
import moped.generic.ParameterShape
import moped.annotations.CommandName
import moped.annotations.ExtraName
import moped.json.JsonEncoder
import moped.json.JsonDecoder
import moped.json.JsonObject
import moped.json.JsonMember
import moped.json.JsonString
import moped.json.JsonArray
import moped.json.ValueResult
import moped.json.JsonCodec

object HelpCommand {
  implicit lazy val parser: CommandParser[HelpCommand] =
    new CodecCommandParser[HelpCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(Nil),
        JsonEncoder.stringJsonEncoder.contramap[HelpCommand](_ => ""),
        JsonDecoder.constant(new HelpCommand())
      )
    )

  def notRecognized(subcommand: String, app: Application): Int = {
    val closestSubcommand =
      Levenshtein.closestCandidate(
        subcommand,
        app.commands.map(_.subcommandName)
      )
    val didYouMean = closestSubcommand match {
      case None => ""
      case Some(candidate) =>
        s"\n\tDid you mean '${app.binaryName} $candidate'?"
    }
    app.error(
      s"no such subcommand '$subcommand'.$didYouMean\n\tTry '${app.binaryName} help' for more information."
    )
    1
  }
}

@CommandName("help", "--help", "-help")
class HelpCommand(
    screenWidth: Int = Terminals.screenWidth(),
    appUsage: Application => Doc = app =>
      Doc.text(s"${app.binaryName} COMMAND [OPTIONS]"),
    appDescription: Application => Doc = _ => Doc.empty,
    appExamples: Application => Doc = _ => Doc.empty
) extends Command {
  override def run(app: Application): Int = {
    app.arguments match {
      case Nil =>
        val usage = appUsage(app)
        if (usage.nonEmpty) {
          app.env.standardOutput.println(s"USAGE:")
          app.env.standardOutput.println(
            usage.indent(2).renderTrim(screenWidth)
          )
        }
        val description = appDescription(app)
        if (description.nonEmpty) {
          if (usage.nonEmpty) app.env.standardOutput.println()
          app.env.standardOutput.println(s"DESCRIPTION:")
          app.env.standardOutput.println(
            description.indent(2).renderTrim(screenWidth)
          )
        }
        if (app.commands.nonEmpty) {
          val rows = app.commands.map { command =>
            command.subcommandName -> command.description
          }
          val message = Doc.tabulate(' ', "  ", rows).indent(2)
          if (usage.nonEmpty) app.out.println()
          app.out.println(s"COMMANDS:")
          app.out.println(message.renderTrim(screenWidth))
          app.out.println(
            (Doc.text(s"See '${app.binaryName} help COMMAND' ") +
              Doc.paragraph(s"for more information on a specific command."))
              .renderTrim(screenWidth)
          )
        }
        val examples = appExamples(app)
        if (examples.nonEmpty) {
          if (app.commands.nonEmpty) app.out.println()
          app.out.println(s"EXAMPLES:")
          app.out.println(examples.indent(2).renderTrim(screenWidth))
        }
        0
      case subcommand :: Nil =>
        app.commands.find(_.matchesName(subcommand)) match {
          case Some(command) =>
            command.helpMessage(app.out, screenWidth)
            0
          case None =>
            HelpCommand.notRecognized(subcommand, app)
        }
      case obtained =>
        app.error(
          s"expected 1 argument but obtained ${obtained.length} arguments " +
            obtained.mkString("'", " ", "'")
        )
        1
    }
  }

}
