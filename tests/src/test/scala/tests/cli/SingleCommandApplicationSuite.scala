package tests.cli

import moped.testkit.MopedSuite

class SingleCommandApplicationSuite extends MopedSuite(SingleCommand.app) {
  checkOutput(
    "basic",
    List(),
    """|info: hello
       |warn: world
       |error: !
       |single COMMAND [OPTIONS]
       |""".stripMargin
  )

  checkOutput(
    "help",
    List("--help"),
    """|OPTIONS
       |  --help: Boolean         Print this help message
       |  --value: String = ""    Optional string option
       |  --flag: Boolean = false Optional boolean option
       |
       |POSITIONAL ARGUMENTS
       |  Arguments to print out, if non-empty
       |""".stripMargin
  )

  checkOutput("--version", List("--version"), "1.0.0")

  checkOutput("-version", List("-version"), "1.0.0")

  checkOutput(
    "positional",
    List("hello", "world"),
    """|arguments=List(hello, world)
       |""".stripMargin
  )

}
