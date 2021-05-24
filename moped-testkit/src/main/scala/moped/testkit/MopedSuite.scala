package moped.testkit

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock

import scala.collection.immutable.Nil

import moped.cli.Application
import moped.cli.CommandParser
import moped.internal.console.Utils
import moped.json.JsonElement
import moped.parsers.JsonParser
import moped.reporters.ConsoleReporter
import moped.reporters.Input
import moped.reporters.Tput
import munit.FunSuite
import munit.Location
import munit.TestOptions
import munit.internal.console.AnsiColors

abstract class MopedSuite(applicationToTest: Application) extends FunSuite {
  def fatalUnknownFields = false
  val reporter: ConsoleReporter = ConsoleReporter(System.out)
  val temporaryDirectory = new DirectoryFixture
  def clock: Clock = new TestingClock()
  def workingDirectory: Path = temporaryDirectory().resolve("workingDirectory")
  def preferencesDirectory: Path = temporaryDirectory().resolve("preferences")
  def cacheDirectory: Path = temporaryDirectory().resolve("cache")
  def dataDirectory: Path = temporaryDirectory().resolve("data")
  def homeDirectory: Path = temporaryDirectory().resolve("home")
  def binDirectory: Path = temporaryDirectory().resolve("bin")
  def manDirectory: Path = temporaryDirectory().resolve("man1")
  def environmentVariables: Map[String, String] =
    Map("PATH" -> binDirectory.toString())
  val app = new ApplicationFixture(applicationToTest)

  class DirectoryFixture extends Fixture[Path]("Directory") {
    private var path: Path = _
    def apply(): Path = path
    override def beforeAll(): Unit = {
      path = Files.createTempDirectory("moped")
    }
    override def afterEach(context: AfterEach): Unit = {
      DeleteVisitor.deleteRecursively(path)
    }
  }

  class ApplicationFixture(app: Application)
      extends Fixture[Application]("Application") {
    private val out = new ByteArrayOutputStream
    private val ps = new PrintStream(out)
    private val tput = Tput.constant(120)
    private val reporter = ConsoleReporter(ps, isColorEnabled = false)
    private def instrumentedApp =
      app
        .withEnv(
          app
            .env
            .withDataDirectory(dataDirectory)
            .withCacheDirectory(cacheDirectory)
            .withPreferencesDirectory(preferencesDirectory)
            .withWorkingDirectory(workingDirectory)
            .withHomeDirectory(homeDirectory)
            .withStandardOutput(ps)
            .withStandardError(ps)
            .withEnvironmentVariables(environmentVariables)
            .withClock(clock)
        )
        .withFatalUnknownFields(fatalUnknownFields)
        .withTput(tput)
        .withReporter(reporter)
        .withMockedProcesses(
          app.mockedProcesses.map(_.withTput(tput).withReporter(reporter))
        )

    def apply(): Application = instrumentedApp
    def reset(): Unit = {
      out.reset()
    }
    def capturedRawOutput: String = {
      out.toString(StandardCharsets.UTF_8.name())
    }
    def capturedOutput: String = {
      AnsiColors.filterAnsi(capturedRawOutput)
    }
    override def beforeEach(context: BeforeEach): Unit = {
      Files.createDirectories(workingDirectory)
      Files.createDirectories(binDirectory)
      reset()
    }
  }

  override def munitFixtures: Seq[Fixture[_]] =
    super.munitFixtures ++ List(temporaryDirectory, app)

  def checkErrorOutput(
      name: TestOptions,
      arguments: List[String],
      expectedOutput: String,
      workingDirectoryLayout: String = ""
  )(implicit loc: munit.Location): Unit = {
    checkOutput(
      name,
      arguments,
      expectedOutput,
      expectedExit = 1,
      workingDirectoryLayout = workingDirectoryLayout
    )
  }

  def checkOutput(
      name: TestOptions,
      arguments: => List[String],
      expectedOutput: String,
      expectedExit: Int = 0,
      workingDirectoryLayout: String = ""
  )(implicit loc: munit.Location): Unit = {
    test(name) {
      if (workingDirectoryLayout.nonEmpty) {
        FileLayout.fromString(workingDirectoryLayout, workingDirectory)
      }
      val exit = app().run(arguments)
      assertEquals(exit, expectedExit, clues(app.capturedOutput))
      assertNoDiff(app.capturedOutput, expectedOutput)
    }
  }

  def runSuccessfully(arguments: List[String]): Unit = {
    runSuccessfully(arguments, app())
  }

  def runSuccessfully(
      arguments: List[String],
      application: Application
  ): Unit = {
    val exit = application.run(arguments)
    assertEquals(exit, 0, clues(app.capturedOutput))
  }

  def checkHelpMessage(expectFile: Path, writeExpectOutput: Boolean = false)(
      implicit loc: munit.Location
  ): Unit = {
    test("help") {
      val expected =
        if (Files.isRegularFile(expectFile))
          Utils.readFile(expectFile)
        else
          ""
      val out = new StringBuilder()
      def loop(prefix: List[String], c: CommandParser[_]): Unit =
        if (c.isHidden)
          ()
        else if (c.nestedCommands.nonEmpty) {
          c.nestedCommands
            .foreach { n =>
              loop(prefix :+ c.subcommandName, n)
            }
        } else {
          app.reset()
          val commands: List[String] = prefix :+ c.subcommandName :+ "--help"
          val exit = app().run(commands)
          assertEquals(exit, 0, clues(commands, app.capturedOutput))
          out
            .append("$ ")
            .append((app().binaryName :: commands).mkString(" "))
            .append("\n")
            .append(app.capturedOutput)
            .append("\n")
        }
      app().commands.foreach(c => loop(Nil, c))
      val obtained = out.toString()
      if (writeExpectOutput) {
        if (isCI) {
          fail("writeExpectOutput must be false when isCI=true")
        }
        Utils.overwriteFile(expectFile, obtained)
        reporter.info(expectFile.toString())
      } else {
        assertNoDiff(obtained, expected, clues(expectFile))
      }
    }
  }

  override def assertNoDiff(obtained: String, expected: String, clue: => Any)(
      implicit loc: Location
  ): Unit = {
    val obtained2 = obtained.replace(temporaryDirectory().toString(), "")
    super.assertNoDiff(
      // NOTE(olafur) workaround for https://github.com/scalameta/munit/issues/179
      if (obtained2 == "\n")
        ""
      else
        obtained2,
      expected,
      clue
    )
  }

  def assertJsonEquals(obtained: JsonElement, expected: JsonElement)(implicit
      loc: munit.Location
  ): Unit =
    if (obtained != expected) {
      val width = 40
      assertNoDiff(obtained.toDoc.render(width), expected.toDoc.render(width))
      assertEquals(obtained, expected)
    }

  def parseJson(json: String): JsonElement = {
    JsonParser.parse(Input.filename("moped.json", json.replace("'", "\""))).get
  }
}
