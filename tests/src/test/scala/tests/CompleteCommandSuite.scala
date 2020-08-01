package tests

import munit.TestOptions

class CompleteCommandSuite extends BaseSuite {
  def checkCompletions(
      name: TestOptions,
      args: List[String],
      expected: String,
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
      assertNoDiff(app.capturedOutput, expected)
    }
  }

  checkCompletions(
    "empty",
    List(),
    ""
  )

  checkCompletions(
    "subcommands",
    List(""),
    """|help
       |echo
       |""".stripMargin
  )

  checkCompletions(
    "echo-empty",
    List("echo", ""),
    "--uppercase"
  )

  checkCompletions(
    "echo-flag",
    List("echo", "-"),
    "--uppercase"
  )

  checkCompletions(
    "echo-uppercase",
    List("echo", "--uppercase"),
    "--uppercase"
  )

  checkCompletions(
    "echo-uppercase",
    List("echo", "--uppercase", ""),
    "--uppercase"
  )
}
