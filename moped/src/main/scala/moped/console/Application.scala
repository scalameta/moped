package moped.console

import java.nio.file.Path
import java.nio.file.Paths

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import dev.dirs.ProjectDirectories
import fansi.Color
import fansi.Str
import moped.commands.HelpCommand
import moped.internal.console.CommandLineParser
import moped.internal.diagnostics.AggregateDiagnostic
import moped.json.DecodingContext
import moped.json.DecodingResult
import moped.json.ErrorResult
import moped.json.ValueResult
import moped.reporters.ConsoleReporter
import moped.reporters.Reporter

case class Application(
    binaryName: String,
    version: String,
    commands: List[CommandParser[_]],
    relativeCommands: List[CommandParser[_]] = Nil,
    arguments: List[String] = Nil,
    relativeArguments: List[String] = Nil,
    preProcessArguments: List[String] => List[String] =
      HelpCommand.swapTrailingHelpFlag,
    projectQualifier: String = "",
    projectOrganization: String = "",
    onEmptyArguments: BaseCommand = new HelpCommand(),
    onNotRecognoziedCommand: BaseCommand = NotRecognizedCommand,
    env: Environment = Environment.default,
    reporter: Reporter = new ConsoleReporter(
      Environment.default.standardOutput
    ),
    token: CancelToken = CancelToken.empty()
) {
  require(binaryName.nonEmpty, "binaryName must be non-empty")
  def out = env.standardOutput
  def err = env.standardError
  def error(message: Str): Unit = {
    env.standardError.println(Color.LightRed("error: ") ++ message)
  }
  def warning(message: Str): Unit = {
    env.standardError.println(Color.LightYellow("warn: ") ++ message)
  }
  def info(message: Str): Unit = {
    env.standardError.println(Color.LightBlue("info: ") ++ message)
  }

  def consumedArguments = arguments.dropRight(relativeArguments.length)

  def projectDirectories: ProjectDirectories =
    ProjectDirectories.from(projectQualifier, projectOrganization, binaryName)
  def configDirectory: Path = Paths.get(projectDirectories.configDir)
  def cacheDirectory: Path = Paths.get(projectDirectories.cacheDir)
  def dataDirectory: Path = Paths.get(projectDirectories.dataDir)
  def preferencesDirectory: Path = Paths.get(projectDirectories.preferenceDir)

  def runAndExitIfNonZero(args: List[String]): Unit = {
    val exit = run(args)
    if (exit != 0) System.exit(exit)
  }
  def run(arguments: List[String]): Int = {
    val args = preProcessArguments(arguments)
    def loop(n: Int, relativeCommands: List[CommandParser[_]]): Future[Int] = {
      val app = this.copy(
        arguments = args,
        relativeArguments = args.drop(n + 1),
        relativeCommands = relativeCommands
      )
      val remainingArguments = args.drop(n)
      remainingArguments match {
        case Nil => onEmptyArguments.runAsFuture(app)
        case subcommand :: tail =>
          relativeCommands.find(_.matchesName(subcommand)) match {
            case Some(command) =>
              if (command.nestedCommands.nonEmpty) {
                loop(
                  n + 1,
                  command.nestedCommands
                )
              } else {
                val conf =
                  CommandLineParser.parseArgs[command.Value](tail)(
                    command.asClassShaper
                  )
                val configured: DecodingResult[BaseCommand] =
                  conf.flatMap(elem =>
                    command.decodeCommand(DecodingContext(elem))
                  )
                configured match {
                  case ValueResult(value) =>
                    value.runAsFuture(app)
                  case ErrorResult(error) =>
                    error.all.foreach {
                      case _: AggregateDiagnostic =>
                      case diagnostic =>
                        app.reporter.log(diagnostic)
                    }
                    Future.successful(1)
                }
              }
            case None =>
              onNotRecognoziedCommand.runAsFuture(app)
          }
      }
    }
    val value = loop(0, commands)
    Await.result(value, Duration.Inf)
  }
}
