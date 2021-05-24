package tests.internal.reporters

import java.io.ByteArrayOutputStream

import fansi.Bold
import fansi.Color
import fansi.Str
import moped.cli.Application
import moped.internal.reporters.NoColorPrintStream
import moped.testkit.MopedSuite
import munit.TestOptions

object NoColorOutputStreamSuite {
  val app: Application =
    Application.simple("noColor") { app =>
      app.info("hello")
      app.out.println(Color.Blue("hello"))
      app.err.println(Color.Green("hello"))
      0
    }
}

class NoColorOutputStreamSuite
    extends MopedSuite(NoColorOutputStreamSuite.app) {
  def checkNoColor(name: TestOptions, original: Str)(implicit
      loc: munit.Location
  ): Unit = {
    test(name) {
      val baos = new ByteArrayOutputStream
      val ps = new NoColorPrintStream(baos)
      ps.print(original)
      ps.flush()
      val obtained = baos.toString()
      assertEquals(obtained, expected = original.plainText)
    }
  }

  checkNoColor("basic", Color.Blue("info: ") ++ "hello")

  checkNoColor("layered", Bold.On(Color.Blue("info: ")) ++ "hello")

  test("NO_COLOR=true") {
    runSuccessfully(
      List(),
      app()
        .withEnv(app().env.withEnvironmentVariables(Map("NO_COLOR" -> "true")))
    )
    assertEquals(app.capturedRawOutput, "info: hello\nhello\nhello\n")
  }

  test("default") {
    runSuccessfully(List())
    assertEquals(
      app.capturedRawOutput,
      List(
        Color.LightBlue("info: ") ++ "hello\n",
        Color.Blue("hello") ++ "\n",
        Color.Green("hello") ++ "\n"
      ).mkString
    )
  }
}
