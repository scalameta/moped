package moped.commands

import java.nio.file.Files
import java.nio.file.StandardOpenOption

import scala.collection.immutable.Nil

import moped.annotations.CatchInvalidFlags
import moped.annotations.Description
import moped.annotations.PositionalArguments
import moped.console.Application
import moped.console.BashCompletion
import moped.console.CodecCommandParser
import moped.console.Command
import moped.console.CommandParser
import moped.console.FishCompletion
import moped.console.ShellCompletion
import moped.console.TabCompletionContext
import moped.console.TabCompletionItem
import moped.console.ZshCompletion
import moped.internal.console.Cases
import moped.internal.console.CommandLineParser
import moped.internal.json.NumberExtractor
import moped.json.JsonArray
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.json.JsonString
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape
import moped.annotations.CommandName

object RunCompletionsCommand {

  def parser(app: Application): CommandParser[RunCompletionsCommand] = {
    val default = new RunCompletionsCommand(app.commands)
    new CodecCommandParser[RunCompletionsCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(
          new ClassShape(
            "CompleteCommand",
            "moped.commands.CompleteCommand",
            List(
              List(
                new ParameterShape(
                  "arguments",
                  "List[String]",
                  List(PositionalArguments(), CatchInvalidFlags()),
                  None
                )
              )
            ),
            List(
              CommandName("run"),
              Description("Print tab completions given command arguments")
            )
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[RunCompletionsCommand](_ => ""),
        JsonDecoder.constant(default)
      ),
      default
    )
  }
}

class RunCompletionsCommand(commands: List[CommandParser[_]]) extends Command {
  def run(app: Application): Int = {
    val (format, argumentLength, arguments) = app.arguments match {
      case _ :: "zsh" :: NumberExtractor(argumentLength) :: tail =>
        (new ZshCompletion(app), argumentLength.toInt, tail)
      case _ :: "bash" :: NumberExtractor(argumentLength) :: tail =>
        (new BashCompletion(app), argumentLength.toInt, tail)
      case _ :: "fish" :: tail =>
        // Fish completions pass in an empty string "" as the last argument so we
        // don't need the second argument to know `tail.length`
        (new FishCompletion(app), tail.length, tail)
      case els =>
        app.error(
          s"invalid arguments $els, to fix this problem pass in '${app.binaryName} run $$SHELL $$ARGUMENTS_LENGTH $$ARGUMENTS'. "
        )
        return 1
    }
    val isMissingTrailingEmptyString =
      argumentLength == arguments.length + 1
    val argumentsWithTrailingEmptyString =
      if (isMissingTrailingEmptyString) arguments :+ ""
      else arguments
    val completionItems =
      completions(argumentsWithTrailingEmptyString, app, argumentLength, format)
    renderCompletions(completionItems, app)
    0
  }

  private def completions(
      arguments: List[String],
      app: Application,
      argumentLength: Int,
      format: ShellCompletion
  ): List[TabCompletionItem] = {
    def loop(args: List[String]): List[TabCompletionItem] =
      args match {
        case _ :: _ :: Nil =>
          commands
            .filterNot(_.isHidden)
            .map(p => TabCompletionItem(p.subcommandName))
        case _ :: subcommandName :: head :: tail =>
          commands.find(_.matchesName(subcommandName)) match {
            case Some(subcommand) =>
              if (subcommand.subcommands.nonEmpty) loop(head :: tail)
              else {
                renderSubcommandCompletions(
                  argumentLength,
                  format,
                  subcommand,
                  head,
                  tail,
                  app
                )
              }
            case None =>
              Nil
          }
        case _ =>
          Nil
      }
    loop(arguments)
  }

  private def renderSubcommandCompletions(
      argumentLength: Int,
      shell: ShellCompletion,
      subcommand: CommandParser[_],
      head: String,
      tail: List[String],
      app: Application
  ): List[TabCompletionItem] = {
    val last = tail.lastOption.getOrElse(head)
    val inlined =
      CommandLineParser.allSettings(subcommand).filter {
        case (_, param) =>
          !param.isHidden ||
            !param.isPositionalArgument
      }
    val secondLast = (head :: tail).takeRight(2) match {
      case flag :: _ :: Nil => Some(flag)
      case _ => None
    }
    val setting = secondLast.flatMap(flag =>
      inlined.get(Cases.kebabToCamel(flag.stripPrefix("--")))
    )
    val context = TabCompletionContext(
      shell,
      argumentLength,
      head :: tail,
      last,
      secondLast,
      setting,
      inlined,
      app
    )
    tabCompletions(subcommand, context)
  }

  private def renderCompletions(
      items: List[TabCompletionItem],
      app: Application
  ): Unit = {
    import scala.collection.JavaConverters._
    val prettyItems =
      JsonArray(items.map(i => JsonString(i.name)).toList).toDoc.render(80)
    Files.write(
      app.env.homeDirectory.resolve(".dump"),
      List(prettyItems).asJava,
      StandardOpenOption.APPEND,
      StandardOpenOption.CREATE
    )
    items.foreach { item =>
      app.out.println(item.name)
    }
  }

  private def tabCompletions(
      command: CommandParser[_],
      context: TabCompletionContext
  ): List[TabCompletionItem] = {
    if (context.last.startsWith("-")) {
      tabCompleteFlags(context)
    } else {
      context.setting match {
        case Some(setting) =>
          if (setting.isTabCompleteOneOf) {
            setting.tabCompleteOneOf.toList.flatten.map(oneof =>
              TabCompletionItem(oneof)
            )
          } else if (setting.isTabComplete) {
            setting.tabCompleter.toList.flatMap(_.complete(context))
          } else {
            command.complete(context)
          }
        case None =>
          command.complete(context)
      }
    }
  }

  private def tabCompleteFlags(
      context: TabCompletionContext
  ): List[TabCompletionItem] = {
    context.allSettings
      .filterNot {
        case (_, setting) => setting.isPositionalArgument
      }
      .keys
      .toList
      .sorted
      .map(camel => TabCompletionItem("--" + Cases.camelToKebab(camel)))
  }

}
