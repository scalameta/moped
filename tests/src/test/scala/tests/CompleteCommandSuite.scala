package tests

import munit.TestOptions

class CompleteCommandSuite extends BaseSuite {
  def checkCompletions(
      name: TestOptions,
      args: List[String],
      expected: List[String],
      format: String = "zsh"
  )(implicit loc: munit.Location): Unit = {
    test(name) {
      val isEmpty = args.nonEmpty && args.last == ""
      val completeArgs = if (isEmpty) args.init else args
      val current = if (isEmpty) args.length + 1 else args.length
      val exit = app().run(
        List(
          "complete",
          format,
          current.toString(),
          app().binaryName
        ) ++ completeArgs
      )
      assertEquals(exit, 0, clues(app.capturedOutput))
      assertEquals(app.capturedOutput.trim.linesIterator.toList, expected)
    }
  }

  checkCompletions(
    "empty",
    List(),
    List()
  )

  checkCompletions(
    "subcommands",
    List(""),
    List("help", "echo")
  )

  checkCompletions(
    "echo-empty",
    List("echo", ""),
    List()
  )

  checkCompletions(
    "echo-flag",
    List("echo", "-"),
    List("--uppercase")
  )

  checkCompletions(
    "echo-uppercase",
    List("echo", "--uppercase"),
    List("--uppercase")
  )

  checkCompletions(
    "echo-uppercase",
    List("echo", "--uppercase", ""),
    List()
  )
  checkCompletions(
    "help-subcommand",
    List("help", ""),
    List("help", "echo")
  )

  checkCompletions(
    "help-subcommand",
    List("help", "e"),
    List("help", "echo")
  )

  checkCompletions(
    "help-subcommand-repeat",
    List("help", "echo", ""),
    List()
  )
}
