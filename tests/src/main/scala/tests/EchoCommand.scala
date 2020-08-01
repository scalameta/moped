package tests

import java.nio.file.Path

import moped.annotations.PositionalArguments
import moped.annotations._
import moped.console.Application
import moped.console.Command
import moped.console.CommandParser
import moped.console.CompleteCommand
import moped.console.HelpCommand

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
  def main(args: Array[String]): Unit = {
    val app = Application(
      "tests",
      "1.0.0",
      commands = List(
        CommandParser[HelpCommand],
        CommandParser[TerminalsCommand],
        CommandParser[EchoCommand],
        CommandParser[CompleteCommand]
      )
    )
    app.runAndExitIfNonZero(args.toList)
  }
}
