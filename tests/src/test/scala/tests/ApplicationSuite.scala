package tests

import moped.json.ValueResult
import moped.console.Application
import moped.console.CommandParser
import moped.console.Command
import moped.annotations.PositionalArguments

case class EchoCommand(
    verbose: Boolean,
    @PositionalArguments()
    args: List[String]
) extends Command("echo") {
  def run(app: Application): Int = {
    val toPrint =
      if (verbose) args.map(_.toUpperCase())
      else args
    app.env.standardOutput.println(toPrint.mkString(" "))
    0
  }
}

class ApplicationSuite extends munit.FunSuite {
  val app = Application(
    "app",
    "1.0.0",
    commands = List(
      CommandParser[EchoCommand](???, ???, ???)
    )
  )

  test("foo") {
    val x = ValueResult(1).upcast
  }
}
