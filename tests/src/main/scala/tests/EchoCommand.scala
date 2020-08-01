package tests

import moped.annotations.PositionalArguments
import moped.annotations._
import moped.console.Application
import moped.console.Command
import moped.console.CommandParser
import moped.console.CompleteCommand
import moped.console.HelpCommand

@Description("Write arguments to the standard output")
@ExampleUsage(
  """|$ echo hello world!
     |hello world!
     |$ echo --uppercase hello world!
     |HELLO WORLD!
     |""".stripMargin
)
case class EchoCommand(
    @Description("If true, the output will be all UPPERCASE")
    uppercase: Boolean = false,
    @PositionalArguments()
    args: List[String] = Nil
) extends Command {
  def run(app: Application): Int = {
    val toPrint =
      if (uppercase) args.map(_.toUpperCase())
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
