package moped.cli

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

import dataclass.data
import dev.dirs.ProjectDirectories
import fansi.Color
import fansi.Str
import moped.annotations.CommandName
import moped.annotations.PositionalArguments
import moped.annotations.TabCompleter
import moped.annotations.TreatInvalidFlagAsPositional
import moped.commands._
import moped.internal.console.CommandLineParser
import moped.internal.console.PathCompleter
import moped.internal.console.StackTraces
import moped.internal.diagnostics.AggregateDiagnostic
import moped.internal.reporters.NoColorPrintStream
import moped.json.AlwaysDerivedParameter
import moped.json.AlwaysHiddenParameter
import moped.json.DecodingContext
import moped.json.ErrorResult
import moped.json.JsonDecoder
import moped.json.JsonElement
import moped.json.JsonEncoder
import moped.json.JsonObject
import moped.json.Result
import moped.json.ValueResult
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape
import moped.parsers.ConfigurationParser
import moped.parsers.JsonParser
import moped.reporters.ConsoleReporter
import moped.reporters.Diagnostic
import moped.reporters.Reporter
import moped.reporters.Terminals
import moped.reporters.Tput
import org.typelevel.paiges.Doc
import os.Shellable

@data
class Application(
    binaryName: String,
    version: String,
    commands: List[CommandParser[_]],
    env: Environment,
    reporter: Reporter,
    tagline: String = "",
    description: Doc = Doc.empty,
    usage: String = s"{BINARY_NAME} COMMAND [OPTIONS]",
    examples: Doc = Doc.empty,
    fatalUnknownFields: Boolean = false,
    relativeCommands: List[CommandParser[_]] = Nil,
    arguments: List[String] = Nil,
    relativeArguments: List[String] = Nil,
    preProcessClassShape: ClassShape => ClassShape = HelpCommand.insertHelpFlag,
    preProcessArguments: List[String] => List[String] = { args =>
      HelpCommand
        .swapTrailingHelpFlag(HelpCommand.moveFlagsBehindSubcommand(args))
    },
    onEmptyArguments: Application => BaseCommand = app => new HelpCommand(app),
    onNotRecognoziedCommand: Application => BaseCommand =
      app => new NotRecognizedCommand(app),
    parsers: List[ConfigurationParser] = List(JsonParser),
    executionContext: ExecutionContext = ExecutionContext.global,
    searcher: ConfigurationSearcher =
      new AggregateSearcher(List(ProjectSearcher, SystemSearcher)),
    token: CancelToken = CancelToken.empty(),
    mockedProcesses: List[Application] = Nil,
    tput: Tput = Tput.system,
    isSingleCommand: Boolean = false
) extends AlwaysDerivedParameter
    with AlwaysHiddenParameter {
  require(binaryName.nonEmpty, "binaryName must be non-empty")
  val terminal = new Terminals(tput)
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
  def println(doc: Doc): Unit = {
    print(doc + Doc.line)
  }
  def println(message: String): Unit = {
    out.println(message)
  }
  def print(doc: Doc): Unit = {
    print(doc.render(terminal.screenWidth()))
  }
  def print(message: String): Unit = {
    out.print(message)
  }
  def printStackTrace(e: Throwable): Unit = {
    StackTraces.trimStackTrace(e).printStackTrace(err)
  }

  def process(command: Shellable*): SpawnableProcess =
    new SpawnableProcess(command, env, mockedProcesses)

  def consumedArguments: List[String] =
    arguments.dropRight(relativeArguments.length)

  def runAndExitIfNonZero(args: List[String]): Unit = {
    val exit = run(args)
    if (exit != 0)
      env.exit(exit)
  }
  def run(arguments: List[String]): Int = {
    if (isSingleCommand)
      runSingleCommand(arguments)
    else
      Application.run(this.withArguments(arguments))
  }
  private def runSingleCommand(arguments: List[String]): Int = {
    val singleCommand = commands
      .find { c =>
        c.subcommandName != "version" && c.subcommandName != "help"
      }
      .orElse(commands.headOption)
    singleCommand match {
      case Some(command) =>
        val newArguments = command.subcommandName :: arguments
        Application.run(this.withArguments(command.subcommandName :: arguments))
      case None =>
        this.error(
          "can't run command since the commands list is empty. " +
            "To fix this problem, update `Application.commands` field to be non-empty."
        )
        1
    }
  }

  // TODO(olafur): avoid this hacky replace.
  def usageDoc: Doc = Doc.text(usage.replace("{BINARY_NAME}", binaryName))

  def documentation: List[(String, Doc)] =
    List[(String, Doc)](
      "USAGE" -> usageDoc,
      "DESCRIPTION" -> description,
      "COMMANDS" -> {
        val rows = relativeCommands.map { command =>
          command.subcommandName -> command.description
        }
        val commands = Doc.tabulate(' ', "  ", rows)
        val moreInfo =
          (Doc.text(s"Use '${binaryName} help COMMAND' ") +
            Doc.paragraph(s"for more information on a specific command."))
        commands + Doc.line + Doc.line + moreInfo
      },
      "EXAMPLES" -> examples
    )
}

object Application {
  implicit val shape: ClassShaper[Application] = ClassShaper[Application](
    new ClassShape(
      "Application",
      "moped.cli.Application",
      parameters = List(
        List(
          new ParameterShape(
            "cwd",
            "Path",
            List(TabCompleter(PathCompleter)),
            None
          )
        ),
        List()
      ),
      List()
    )
  )
  val default: Application = Application.fromName(
    "moped-default-application-please-change-me",
    "moped-default-version-please-change-me",
    List()
  )

  def fromName(
      binaryName: String,
      version: String,
      commands: List[CommandParser[_]]
  ): Application = {
    val env = Environment
      .fromProjectDirectories(ProjectDirectories.fromPath(binaryName))
    Application(
      binaryName,
      version,
      commands,
      env = env,
      reporter = ConsoleReporter(env.standardOutput, env.isColorEnabled)
    )
  }

  def simple(binaryName: String)(fn: Application => Int): Application = {
    single(binaryName, app => new SimpleCommand(app, fn))
  }

  def single(
      binaryName: String,
      command: Application => BaseCommand,
      extraCommands: List[CommandParser[_]] = List(
        CommandParser[HelpCommand],
        CommandParser[VersionCommand]
      )
  ): Application = {
    val singleCommand: CommandParser[_] =
      new CommandParser[BaseCommand](
        JsonEncoder.unitJsonEncoder.contramap[BaseCommand](_ => ()),
        JsonDecoder.applicationJsonDecoder.map(a => command(a)),
        command(Application.default),
        ClassShape(
          binaryName,
          binaryName,
          List(
            List(
              new ParameterShape(
                "arguments",
                "List[String]",
                List(
                  new PositionalArguments(),
                  new TreatInvalidFlagAsPositional()
                ),
                None
              )
            )
          ),
          List(new CommandName(binaryName))
        )
      )
    val app = Application
      .fromName(
        binaryName,
        version = "1.0.0",
        commands = singleCommand :: extraCommands
      )
      .withIsSingleCommand(true)
    app
  }

  def run(app: Application): Int = {
    implicit val ec = app.executionContext
    val args = app.preProcessArguments(app.arguments)
    val base = app
      .withCommands(app.commands.map(_.withApplication(app)))
      .withEnv(
        if (app.env.isColorEnabled)
          app.env
        else
          app
            .env
            .withStandardOutput(new NoColorPrintStream(app.env.standardOutput))
            .withStandardError(new NoColorPrintStream(app.env.standardError))
      )
    def onError(error: Diagnostic): Future[Int] = {
      error
        .all
        .foreach {
          case _: AggregateDiagnostic =>
          case diagnostic =>
            app.reporter.log(diagnostic)
        }
      Future.successful(1)
    }
    def loop(
        n: Int,
        relativeCommands: List[CommandParser[_]],
        baseCommand: Option[CommandParser[_]]
    ): Future[Int] = {
      val app = base
        .withArguments(args)
        .withRelativeArguments(args.drop(n + 1))
        .withRelativeCommands(relativeCommands)
      val remainingArguments = args.drop(n)
      remainingArguments match {
        case Nil =>
          baseCommand match {
            case Some(parser) =>
              parser.decodeCommand(
                DecodingContext(JsonObject(Nil), app)
                  .withFatalUnknownFields(app.fatalUnknownFields)
              ) match {
                case ValueResult(cmd) =>
                  cmd.runAsFuture()
                case ErrorResult(error) =>
                  onError(error)
              }
            case None =>
              app.onEmptyArguments.apply(app).runAsFuture()
          }
        case subcommand :: tail =>
          relativeCommands.find(_.matchesName(subcommand)) match {
            case Some(command) =>
              if (command.nestedCommands.nonEmpty) {
                loop(
                  n + 1,
                  command.nestedCommands,
                  Some[CommandParser[_]](command)
                )
              } else {
                val conf: Result[JsonObject] =
                  CommandLineParser
                    .parseArgs[command.Value](tail)(command.asClassShaper)
                for {
                  parsedConfig <- app.searcher.findAsync(app)
                  configs = Result.fromResults(conf :: parsedConfig)
                  mergedConfig = configs.map(JsonElement.merge)
                  configured = mergedConfig.flatMap(elem =>
                    command.decodeCommand(
                      DecodingContext(elem, app)
                        .withFatalUnknownFields(app.fatalUnknownFields)
                    )
                  )
                  exit <-
                    configured match {
                      case ValueResult(value) =>
                        value.runAsFuture()
                      case ErrorResult(error) =>
                        onError(error)
                    }
                } yield exit
              }
            case None =>
              app.onNotRecognoziedCommand(app).runAsFuture()
          }
      }
    }
    try {
      val future = loop(0, base.commands, None)
      val exit =
        future.value match {
          case Some(value) =>
            value.get
          case None =>
            Await.result(future, Duration.Inf)
        }
      exit
    } catch {
      case NonFatal(e) =>
        e.printStackTrace(app.err)
        1
    } finally {
      app.out.flush()
      app.err.flush()
    }
  }
}
