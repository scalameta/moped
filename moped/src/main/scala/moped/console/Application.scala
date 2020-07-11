package moped.console

import scala.concurrent.Promise
import scala.runtime.EmptyMethodCache
import fansi.Str
import fansi.Color
import moped.internal.console.HelpMessage
import moped.internal.console.CommandLineParser
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import moped.json.DecodingResult
import moped.json.ValueResult
import moped.json.ErrorResult
import moped.json.DecodingContext
import moped.internal.diagnostics.AggregateDiagnostic
import moped.reporters.Reporter
import moped.reporters.ConsoleReporter

case class Application(
    binaryName: String,
    version: String,
    commands: List[CommandParser[_]],
    arguments: List[String] = Nil,
    onEmptyArguments: BaseCommand = new HelpCommand(),
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
            Future.successful(HelpCommand.notRecognized(subcommand, app))
        }
    }
    Await.result(f, Duration.Inf)
  }
}
