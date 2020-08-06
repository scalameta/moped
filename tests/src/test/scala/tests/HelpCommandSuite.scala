package tests

import munit.TestOptions
import moped.commands.HelpCommand

class HelpCommandSuite extends BaseSuite {
  checkHelpMessage(
    BuildInfo.expectDirectory.toPath().resolve("tests.help.txt"),
    writeExpectOutput = false
  )

  def checkMoveFlag(
      name: TestOptions,
      original: List[String],
      expected: List[String]
  )(implicit loc: munit.Location): Unit = {
    test(name) {
      val obtained = HelpCommand.moveFlagsBehindSubcommand(original)
      assertEquals(obtained, expected)
    }
  }

  checkMoveFlag(
    "unchanged",
    List("echo", "--uppercase", "hello world"),
    List("echo", "--uppercase", "hello world")
  )

  checkMoveFlag(
    "unchanged",
    List("--echo", "--uppercase", "--hello world"),
    List("--echo", "--uppercase", "--hello world")
  )

  checkMoveFlag(
    "move-one",
    List("--uppercase", "echo", "hello world"),
    List("echo", "--uppercase", "hello world")
  )

  checkMoveFlag(
    "move-two",
    List("--uppercase", "--uppercase", "echo", "hello world"),
    List("echo", "--uppercase", "--uppercase", "hello world")
  )

  checkMoveFlag(
    "move-value",
    List("--uppercase=true", "echo", "hello world"),
    List("echo", "--uppercase=true", "hello world")
  )
}
