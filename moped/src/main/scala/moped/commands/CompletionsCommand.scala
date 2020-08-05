package moped.commands
import java.nio.file.StandardOpenOption

import scala.collection.immutable.Nil

import moped.annotations.CatchInvalidFlags
import moped.annotations.Description
import moped.annotations.Hidden
import moped.annotations.PositionalArguments
import moped.console.Application
import moped.console.BashCompletion
import moped.console.CodecCommandParser
import moped.console.Command
import moped.console.CommandParser
import moped.console.FishCompletion
import moped.console.ShellCompletion
import moped.console.TabCompletionContext
import moped.console.TabCompletionItem
import moped.console.ZshCompletion
import moped.internal.console.Cases
import moped.internal.console.CommandLineParser
import moped.internal.json.NumberExtractor
import moped.json.JsonArray
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.json.JsonString
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape
import moped.annotations.CommandName
import moped.console.InstallCompletionsCommand
import moped.console.UninstallCompletionsCommand

object CompletionsCommand {
  val default = new CompletionsCommand()

  implicit lazy val parser: CommandParser[CompletionsCommand] =
    new CodecCommandParser[CompletionsCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(
          new ClassShape(
            "CompleteCommand",
            "moped.commands.CompleteCommand",
            List(
              List(
                new ParameterShape(
                  "arguments",
                  "List[String]",
                  List(PositionalArguments(), CatchInvalidFlags()),
                  None
                )
              )
            ),
            List(
              CommandName("completions"),
              Description("Manage tab completions for bash, zsh and fish")
            )
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[CompletionsCommand](_ => ""),
        JsonDecoder.constant(default)
      ),
      default
    )
}

class CompletionsCommand extends Command {

  override def run(app: Application): Int = {
    val completionsApp = app.copy(
      binaryName = s"${app.binaryName} completions",
      commands = List(
        CommandParser[HelpCommand],
        CommandParser[InstallCompletionsCommand],
        CommandParser[UninstallCompletionsCommand],
        RunCompletionsCommand.parser(app)
      )
    )
    app.arguments match {
      case _ :: arguments =>
        completionsApp.run(arguments)
      case Nil =>
        app.error("missing arguments")
        1
    }
  }

}
