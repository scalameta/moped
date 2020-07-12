package moped.console

import scala.collection.immutable.Nil
import moped.internal.console.CommandLineParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import moped.internal.console.Cases
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.macros.ClassShaper
import moped.macros.ClassShape
import moped.macros.ParameterShape
import moped.annotations.PositionalArguments
import moped.json.JsonObject
import moped.internal.json.DrillIntoJson
import moped.annotations.ParseAsNumber

object CompleteCommand {

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
                  "current",
                  "Option[Int]",
                  List(new ParseAsNumber()),
                  None
                ),
                new ParameterShape("format", "Option[String]", Nil, None),
                new ParameterShape(
                  "arguments",
                  "List[String]",
                  List(new PositionalArguments()),
                  None
                )
              )
            ),
            Nil
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[CompleteCommand](_ => ""),
        JsonDecoder.fromJson("JsonObject") {
          case obj: JsonObject =>
            val current = DrillIntoJson.get[Int](obj, "current")
            val format = DrillIntoJson.get[String](obj, "format")
            val arguments = DrillIntoJson
              .get[List[String]](obj, CommandLineParser.PositionalArgument)
            current.product(format).product(arguments).map {
              case ((a, b), c) =>
                CompleteCommand(a, b, c)
            }
        }
      )
    )
}

case class CompleteCommand(
    current: Int,
    format: String,
    arguments: List[String]
) extends Command {

  override def isHidden: Boolean = true
  def run(app: Application): Int = {
    pprint.log(current)
    pprint.log(format)
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
