package tests.internal.console

import moped.internal.console.Utils
import moped.testkit.MopedSuite
import tests.EchoCommand

class UtilsSuite extends MopedSuite(EchoCommand.app) {
  test("overwriteFile") {
    val tmp = temporaryDirectory().resolve("tmp")
    val message = "Hello world"
    Utils.overwriteFile(tmp, message)
    assertNoDiff(Utils.readFile(tmp), message)
  }
}
