package tests

class CommandLineParserSuite extends BaseSuite {
  checkErrorOutput(
    "unexpected-flag",
    List("echo", "--foobar", "hello world"),
    """|error: found argument '--foobar' which wasn't expected, or isn't valid in this context.
       |""".stripMargin
  )
}
