package tests

import moped.console.Command
import moped.console.Application
import moped.reporters.Terminals
import moped.console.CommandParser

final case class TerminalsCommand() extends Command {
  def run(app: Application): Int = {
    app.out.println(Terminals.screenWidth())
    0
  }
}

object TerminalsCommand {
  implicit val parser = CommandParser.derive(TerminalsCommand())
}
