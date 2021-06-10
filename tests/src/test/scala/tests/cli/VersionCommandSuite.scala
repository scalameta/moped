package tests.cli

import moped.testkit.MopedSuite
import tests.TestsApplication

class VersionCommandSuite extends MopedSuite(TestsApplication.app) {

  checkOutput("--version", List("--version"), "1.0.0")

  checkOutput("-version", List("-version"), "1.0.0")

  checkOutput("version", List("version"), "1.0.0")

}
