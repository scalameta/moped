package moped.commands
import scala.collection.immutable.Nil

import moped.annotations.CatchInvalidFlags
import moped.annotations.CommandName
import moped.annotations.Description
import moped.annotations.PositionalArguments
import moped.console.Application
import moped.console.CodecCommandParser
import moped.console.Command
import moped.console.CommandParser
import moped.console.InstallCompletionsCommand
import moped.console.UninstallCompletionsCommand
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape

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
    ) {
      override def subcommands(app: Application): List[CommandParser[_]] =
        List(
          CommandParser[HelpCommand],
          CommandParser[InstallCompletionsCommand],
          CommandParser[UninstallCompletionsCommand],
          RunCompletionsCommand.parser(app)
        )
    }
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
