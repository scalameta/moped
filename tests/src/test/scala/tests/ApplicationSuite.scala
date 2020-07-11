package tests

import moped.json.ValueResult
import moped.console.Application
import moped.console.CommandParser
import moped.console.Command
import moped.annotations.PositionalArguments
import moped.json.JsonCodec
import moped.annotations.CommandName
import moped.annotations.TabComplete
import moped.console.TabCompletionItem
import moped.console.HelpCommand

@CommandName("echo")
@TabComplete(context => List(TabCompletionItem("hello world")))
case class EchoCommand(
    verbose: Boolean = false,
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

class ApplicationSuite extends munit.FunSuite {
  val app = Application(
    "app",
    "1.0.0",
    commands = List(
      CommandParser[HelpCommand],
      CommandParser[EchoCommand]
    )
  )

  test("foo") {
    // app.run(List("echo", "--verbose", "Hello world!"))
  }
}
