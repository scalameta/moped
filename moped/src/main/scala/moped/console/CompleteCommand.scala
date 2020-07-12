package moped.console

import scala.collection.immutable.Nil
import moped.internal.console.CommandLineParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import moped.internal.console.Cases

case class CompleteCommand(
    current: Option[Int] = None,
    format: Option[String] = None,
    arguments: List[String] = Nil
) extends Command {

  override def isHidden: Boolean = true
  def run(app: Application): Int = {
    val isMissingTrailingEmptyString =
      current.contains(arguments.length + 1)
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
          renderSubcommandCompletions(subcommand, head, tail, app)
        }
      case _ =>
    }
    0
  }

  private def renderSubcommandCompletions(
      subcommand: CommandParser[_],
      head: String,
      tail: List[String],
      app: Application
  ): Unit = {
    val last = tail.lastOption.getOrElse(head)
    val inlined =
      CommandLineParser.allSettings(subcommand).filter(!_._2.isHidden)
    val secondLast = (head :: tail).takeRight(2) match {
      case flag :: last :: Nil => Some(flag)
      case _                   => None
    }
    val setting = secondLast.flatMap(flag =>
      inlined.get(Cases.kebabToCamel(flag.stripPrefix("--")))
    )
    val context = TabCompletionContext(
      format,
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
