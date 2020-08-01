package tests

import munit.TestOptions

class CompleteCommandSuite extends BaseSuite {
  def checkCompletions(
      name: TestOptions,
      args: List[String],
      expected: String,
      format: String = "zsh"
  )(implicit
      loc: munit.Location
  ): Unit = {
    test(name) {
      val isEmpty = args.last == ""
      val completeArgs = if (isEmpty) args.init else args
      val current = if (isEmpty) args.length + 1 else args.length
      val exit = app().run(
        List(
          "complete",
          "--current",
          current.toString(),
          "--format",
          format
        ) ++ completeArgs
      )
      assertEquals(exit, 0, clues(app.capturedOutput))
      assertNoDiff(app.capturedOutput, expected)
    }
  }

  checkCompletions(
    "basic",
    List(""),
    """|
       |""".stripMargin
  )
}
