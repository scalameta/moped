package tests

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
