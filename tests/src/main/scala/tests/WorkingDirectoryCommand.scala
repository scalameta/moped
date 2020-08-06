package tests

import java.nio.file.Path

import moped.console.Application
import moped.console.Command
import moped.console.CommandParser

object WorkingDirectoryCommand {
  implicit val parser: CommandParser[WorkingDirectoryCommand] = CommandParser.derive(WorkingDirectoryCommand())
}

case class WorkingDirectoryCommand(
    home: Option[Path] = None
) extends Command {

  override def run(app: Application): Int = ???

}
