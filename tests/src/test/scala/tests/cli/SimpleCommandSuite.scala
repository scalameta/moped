package tests.cli

import moped.cli.Application
import moped.testkit.MopedSuite

object SimpleCommandSuite {
  def boom(): Nothing = throw new RuntimeException("boom")
  val app: Application =
    Application.simple("simple")(app => {
      app.arguments match {
        case "boom" :: Nil =>
          boom()
        case _ =>
          app.println(app.arguments.toString())
          0
      }
    })
}

class SimpleCommandSuite extends MopedSuite(SimpleCommandSuite.app) {
  checkOutput("basic", List("a", "b"), "List(a, b)")
  checkErrorOutput(
    "exception",
    List("boom"),
    """|java.lang.RuntimeException: boom
       |	at tests.cli.SimpleCommandSuite$.boom(SimpleCommandSuite.scala:7)
       |	at tests.cli.SimpleCommandSuite$.$anonfun$app$1(SimpleCommandSuite.scala:12)
       |	at tests.cli.SimpleCommandSuite$.$anonfun$app$1$adapted(SimpleCommandSuite.scala:9)
       |	at moped.cli.SimpleCommand.$anonfun$run$1(SimpleCommand.scala:18)
       |""".stripMargin
  )
}
