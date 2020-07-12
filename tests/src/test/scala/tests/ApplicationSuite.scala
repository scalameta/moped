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

object Kind extends Enumeration {
  val A, B, C = Value
}

@CommandName("echo")
case class EchoCommand(
    @Description("If true, blah")
    verbose: Boolean = false,
    kind: Kind.Value = Kind.A,
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
    pprint.log(EchoCommand.parser.parameters)
    pprint.log(implicitly[Completer[Option[Path]]])
    "my-cli --verbose echo foo"
    // app.run(List("echo", "--verbose", "Hello world!"))
  }
}
