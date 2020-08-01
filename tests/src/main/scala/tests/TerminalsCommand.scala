package tests

import moped.console.Command
import moped.console.Application
import moped.reporters.Terminals
import moped.console.CommandParser
import moped.annotations.Description

@Description("Print the console screen width")
final case class TerminalsCommand() extends Command {
  def run(app: Application): Int = {
    app.out.println(Terminals.screenWidth())
    0
  }
}

object TerminalsCommand {
  implicit val parser = CommandParser.derive(TerminalsCommand())
}
