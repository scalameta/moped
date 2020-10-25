package tests.cli

import moped.annotations.Description
import moped.annotations.PositionalArguments
import moped.cli.Application
import moped.cli.Command
import moped.cli.CommandParser
import moped.commands.HelpCommand
import moped.commands.VersionCommand

case class SingleCommand(
    app: Application = Application.default,
    @Description("Optional string option") value: String = "",
    @Description("Optional boolean option") flag: Boolean = false,
    @PositionalArguments
    @Description("Arguments to print out, if non-empty") args: List[String] =
      Nil
) extends Command {
  def run(): Int = {
    if (args.isEmpty) {
      app.info("hello")
      app.warning("world")
      app.error("!")
      app.println(app.usageDoc)
    } else {
      app.println(s"arguments=${args}")
    }
    0
  }
}

object SingleCommand {
  implicit val parser: CommandParser[SingleCommand] = CommandParser
    .derive(SingleCommand())
  val app: Application = Application
    .fromName(
      "single",
      version = "1.0.0",
      commands =
        parser ::
          List(CommandParser[HelpCommand], CommandParser[VersionCommand])
    )
    .copy(isSingleCommand = true)
}
