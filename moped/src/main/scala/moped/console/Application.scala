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

  def run(args: List[String]): Int = {
    val app = this.copy(arguments = args)
    val f: Future[Int] = args match {
      case Nil => onEmptyArguments.runAsFuture(app)
      case subcommand :: tail =>
        pprint.log(subcommand)
        pprint.log(commands.map(_.subcommandNames))
        pprint.log(commands.map(_.subcommandName))
        commands.find(_.matchesName(subcommand)) match {
          case Some(command) =>
            val conf =
              CommandLineParser.parseArgs[command.Value](tail)(
                command.asClassDefinition
              )
            pprint.log(command.subcommandName)
            pprint.log(conf)
            val configured: DecodingResult[BaseCommand] =
              conf.flatMap(elem => command.decodeCommand(DecodingContext(elem)))
            pprint.log(configured)
            configured match {
              case ValueResult(value) =>
                value.runAsFuture(app)
              case ErrorResult(error) =>
                pprint.log(error.all)
                error.all.foreach {
                  case _: AggregateDiagnostic =>
                  case diagnostic =>
                    app.reporter.log(diagnostic)
                }
                Future.successful(1)
            }
          case None =>
            pprint.log(commands.map(_.subcommandName))
            onNotRecognoziedCommand.runAsFuture(app)
        }
    }
    Await.result(f, Duration.Inf)
  }
}
