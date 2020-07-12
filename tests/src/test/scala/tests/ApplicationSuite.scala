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

class ApplicationSuite extends munit.FunSuite {

  test("foo") {
    pprint.log(EchoCommand.parser.parameters)
    pprint.log(implicitly[Completer[Option[Path]]])
    "my-cli --verbose echo foo"
    // app.run(List("echo", "--verbose", "Hello world!"))
  }
}
