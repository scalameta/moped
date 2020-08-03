package tests

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import moped.console.Application
import moped.console.CommandParser
import moped.commands.CompleteCommand
import moped.console.Environment
import moped.commands.HelpCommand
import munit.FunSuite

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
    private val app = Application(
      "app",
      "1.0.0",
      env = Environment(
        standardOutput = ps,
        standardError = ps
      ),
      commands = List(
        CommandParser[HelpCommand],
        CommandParser[CompleteCommand],
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

}
