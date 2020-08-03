package moped.console

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import fansi.Color
import fansi.Str
import moped.internal.console.CommandLineParser
import moped.internal.diagnostics.AggregateDiagnostic
import moped.json.DecodingContext
import moped.json.DecodingResult
import moped.json.ErrorResult
import moped.json.ValueResult
import moped.reporters.ConsoleReporter
import moped.reporters.Reporter
import moped.commands.HelpCommand

case class Application(
    binaryName: String,
    version: String,
    commands: List[CommandParser[_]],
    arguments: List[String] = Nil,
    onEmptyArguments: BaseCommand = new HelpCommand(),
    onNotRecognoziedCommand: BaseCommand = NotRecognizedCommand,
    env: Environment = Environment.default,
    reporter: Reporter = new ConsoleReporter(
      Environment.default.standardOutput
    ),
    token: CancelToken = CancelToken.empty()
) {
  def out = env.standardOutput
  def err = env.standardError
  def error(message: Str): Unit = {
    env.standardError.println(Color.LightRed("error: ") ++ message)
  }
  def warn(message: Str): Unit = {
    env.standardError.println(Color.LightYellow("warn: ") ++ message)
  }
  def info(message: Str): Unit = {
    env.standardError.println(Color.LightBlue("info: ") ++ message)
  }

  def runAndExitIfNonZero(args: List[String]): Unit = {
    val exit = run(args)
    if (exit != 0) System.exit(exit)
  }
  def run(args: List[String]): Int = {
    val app = this.copy(arguments = args)
    val f: Future[Int] = args match {
      case Nil => onEmptyArguments.runAsFuture(app)
      case subcommand :: tail =>
        commands.find(_.matchesName(subcommand)) match {
          case Some(command) =>
            val conf =
              CommandLineParser.parseArgs[command.Value](tail)(
                command.asClassDefinition
              )
            val configured: DecodingResult[BaseCommand] =
              conf.flatMap(elem => command.decodeCommand(DecodingContext(elem)))
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
          case None =>
            onNotRecognoziedCommand.runAsFuture(app)
        }
    }
    Await.result(f, Duration.Inf)
  }
}
