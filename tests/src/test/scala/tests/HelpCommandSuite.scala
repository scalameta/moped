package tests

class HelpCommandSuite extends BaseSuite {
  checkHelpMessage(
    BuildInfo.expectDirectory.toPath().resolve("tests.help.txt"),
    writeExpectOutput = true
  )
}
