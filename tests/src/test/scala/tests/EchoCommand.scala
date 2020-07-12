package tests

import moped.json.ValueResult
import moped.console.Application
import moped.console.CommandParser
import moped.console.Command
import moped.annotations.PositionalArguments
import moped.json.JsonCodec
import moped.annotations._
import moped.console.TabCompletionItem
import moped.console.HelpCommand
import java.nio.file.Path
import moped.console.Completer

case class EchoCommand(
    @Description("If true, blah")
    verbose: Boolean = false,
    path: List[Path] = Nil,
    @PositionalArguments()
    args: List[String] = Nil
) extends Command {
  def run(app: Application): Int = {
    val toPrint =
      if (verbose) args.map(_.toUpperCase())
      else args
    app.env.standardOutput.println(toPrint.mkString(" "))
    0
  }
}

object EchoCommand {
  implicit val parser = CommandParser.derive(EchoCommand())
}
