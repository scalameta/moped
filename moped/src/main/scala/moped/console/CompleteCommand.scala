package moped.console

import scala.collection.immutable.Nil

import moped.annotations.CatchInvalidFlags
import moped.annotations.Description
import moped.annotations.Hidden
import moped.annotations.PositionalArguments
import moped.internal.console.Cases
import moped.internal.console.CommandLineParser
import moped.internal.json.NumberExtractor
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape

sealed abstract class CompletionShell
case object ZshShell extends CompletionShell
case object BashShell extends CompletionShell

object CompleteCommand {
  val default = new CompleteCommand()

  implicit lazy val parser: CommandParser[CompleteCommand] =
    new CodecCommandParser[CompleteCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(
          new ClassShape(
            "CompleteCommand",
            "moped.console.CompleteCommand",
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
              Hidden(),
              Description("Print tab completions for bash or zsh")
            )
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[CompleteCommand](_ => ""),
        JsonDecoder.constant(default)
      ),
      default
    )
}

case class CompleteCommand() extends Command {
  def run(app: Application): Int = {
    val (format, current, arguments) = app.arguments match {
      case _ :: "zsh" :: NumberExtractor(current) :: tail =>
        (ZshShell, current.toInt, tail)
      case els =>
        app.error(
          s"invalid arguments, to fix this problem pass in '${app.binaryName} complete zsh $$CURRENT $$ARGUMENTS'. " +
            s"Other shells like bash are not supported at the moment."
        )
        return 1
    }
    val isMissingTrailingEmptyString =
      current == arguments.length + 1
    val argumentsWithTrailingEmptyString =
      if (isMissingTrailingEmptyString) arguments :+ ""
      else arguments
    argumentsWithTrailingEmptyString match {
      case _ :: _ :: Nil =>
        renderCompletions(
          app.commands.filterNot(_.isHidden).map(_.subcommandName),
          app
        )
      case _ :: subcommandName :: head :: tail =>
        app.commands.find(_.matchesName(subcommandName)).foreach { subcommand =>
          renderSubcommandCompletions(
            current,
            format,
            subcommand,
            head,
            tail,
            app
          )
        }
      case _ =>
    }
    0
  }

  private def renderSubcommandCompletions(
      current: Int,
      shell: CompletionShell,
      subcommand: CommandParser[_],
      head: String,
      tail: List[String],
      app: Application
  ): Unit = {
    val last = tail.lastOption.getOrElse(head)
    val inlined =
      CommandLineParser.allSettings(subcommand).filter(!_._2.isHidden)
    val secondLast = (head :: tail).takeRight(2) match {
      case flag :: _ :: Nil => Some(flag)
      case _ => None
    }
    val setting = secondLast.flatMap(flag =>
      inlined.get(Cases.kebabToCamel(flag.stripPrefix("--")))
    )
    val context = TabCompletionContext(
      shell,
      current,
      head :: tail,
      last,
      secondLast,
      setting,
      inlined,
      app
    )
    tabCompletions(subcommand, context).foreach { item =>
      renderCompletion(item, app)
    }
  }
  private def renderCompletions(items: List[String], app: Application): Unit = {
    items.foreach { item => renderCompletion(TabCompletionItem(item), app) }
  }

  private def renderCompletion(
      item: TabCompletionItem,
      app: Application
  ): Unit = {
    app.out.println(item.name)
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
            tabCompleteFallback(command, context)
          }
        case None =>
          tabCompleteFallback(command, context)
      }
    }
  }

  private def tabCompleteFallback(
      command: CommandParser[_],
      context: TabCompletionContext
  ): List[TabCompletionItem] = {
    val fromCommand = command.complete(context)
    if (fromCommand.isEmpty && context.last == "") {
      tabCompleteFlags(context)
    } else {
      fromCommand
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
