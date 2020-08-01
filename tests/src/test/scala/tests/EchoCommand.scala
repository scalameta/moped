package tests

import java.nio.file.Path

import moped.annotations.PositionalArguments
import moped.annotations._
import moped.console.Application
import moped.console.Command
import moped.console.CommandParser

case class EchoCommand(
    @Description("If true, the output will be all UPPERCASE")
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
  implicit val parser: CommandParser[EchoCommand] =
    CommandParser.derive(EchoCommand())
}
