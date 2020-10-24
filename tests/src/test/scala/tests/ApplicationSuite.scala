package tests

class ApplicationSuite extends BaseSuite {

  checkOutput(
    "global-flag",
    List("--uppercase", "echo", "hello world"),
    "HELLO WORLD"
  )

  checkOutput(
    "no-flag",
    List("echo", "--no-unchanged", "hello world"),
    "--no-unchanged"
  )

  checkOutput(
    "no-default-flag",
    List("echo", "--unchanged", "hello world"),
    "hello world"
  )

  checkOutput(
    "no-no-flag",
    List("echo", "--no-lowercase", "HELLO WORLD"),
    "HELLO WORLD"
  )

  checkOutput(
    "no-flip-flag",
    List("echo", "--lowercase", "HELLO WORLD"),
    "hello world"
  )

  checkErrorOutput(
    "no-no-error",
    List("echo", "--no-no-lowercase", "HELLO WORLD"),
    """|error: found argument '--no-no-lowercase' which wasn't expected, or isn't valid in this context.
       |	Did you mean '--lowercase'?
       |""".stripMargin
  )

  checkOutput(
    "trailing-argument",
    List("echo", "hello", "--uppercase", "--", "world", "--uppercase", "--"),
    "HELLO\n-- world --uppercase --"
  )

  checkOutput(
    "cwd",
    List("example-cwd", "--cwd", "custom-working-directory"),
    "cwd=custom-working-directory"
  )

  checkOutput(
    "app.cwd",
    List("example-cwd", "--app.cwd", "custom-working-directory"),
    "cwd=custom-working-directory"
  )

  checkOutput(
    "shared.app.cwd",
    List("example-cwd", "--shared.app.cwd", "custom-working-directory"),
    "cwd=custom-working-directory"
  )
}
