package tests

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import moped.commands.CompletionsCommand
import moped.commands.HelpCommand
import moped.commands.VersionCommand
import moped.console.Application
import moped.console.CommandParser
import moped.console.Environment
import munit.FunSuite
import munit.TestOptions
import moped.reporters.ConsoleReporter

abstract class BaseSuite extends FunSuite {

  class PathFixture extends Fixture[Path]("Path") {
    private var path: Path = _
    def apply(): Path = path
    override def beforeAll(): Unit = {
      path = Files.createTempDirectory("moped")
    }
    override def afterEach(context: AfterEach): Unit = {
      DeleteVisitor.deleteRecursively(path)
    }
  }

  class ApplicationFixture extends Fixture[Application]("Application") {
    private val out = new ByteArrayOutputStream
    private val ps = new PrintStream(out)
    private val env = Environment(
      standardOutput = ps,
      standardError = ps
    )
    private val app = Application(
      "app",
      "1.0.0",
      env = env,
      reporter = new ConsoleReporter(env.standardOutput),
      commands = List(
        CommandParser[HelpCommand],
        CommandParser[VersionCommand],
        CommandParser[CompletionsCommand],
        CommandParser[WorkingDirectoryCommand],
        CommandParser[EchoCommand]
      )
    )
    def apply(): Application = app
    def capturedOutput: String = out.toString(StandardCharsets.UTF_8.name())
    override def beforeEach(context: BeforeEach): Unit = {
      out.reset()
    }
  }

  val path = new PathFixture
  val app = new ApplicationFixture

  override def munitFixtures: Seq[Fixture[_]] =
    super.munitFixtures ++ List(
      path,
      app
    )

  def checkErrorOutput(
      name: TestOptions,
      arguments: List[String],
      expectedOutput: String
  ): Unit = {
    checkOutput(name, arguments, expectedOutput, expectedExit = 1)
  }
  def checkOutput(
      name: TestOptions,
      arguments: List[String],
      expectedOutput: String,
      expectedExit: Int = 0
  ): Unit = {
    test(name) {
      val exit = app().run(arguments)
      assertEquals(exit, expectedExit, clues(app.capturedOutput))
      assertNoDiff(app.capturedOutput, expectedOutput)
    }
  }

}
