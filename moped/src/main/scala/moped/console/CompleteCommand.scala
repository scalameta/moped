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
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import moped.json.JsonArray
import moped.json.JsonString

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
    val (format, argumentLength, arguments) = app.arguments match {
      case _ :: "zsh" :: NumberExtractor(argumentLength) :: tail =>
        (ZshCompletion, argumentLength.toInt, tail)
      case _ :: "bash" :: NumberExtractor(argumentLength) :: tail =>
        (BashCompletion, argumentLength.toInt, tail)
      case _ :: "fish" :: tail =>
        // Fish completions pass in an empty string "" as the last argument so we
        // don't need the second argument to know `tail.length`
        (FishCompletion, tail.length, tail)
      case els =>
        app.error(
          s"invalid arguments $els, to fix this problem pass in '${app.binaryName} complete $$SHELL $$ARGUMENTS_LENGTH $$ARGUMENTS'. "
        )
        return 1
    }
    val isMissingTrailingEmptyString =
      argumentLength == arguments.length + 1
    val argumentsWithTrailingEmptyString =
      if (isMissingTrailingEmptyString) arguments :+ ""
      else arguments
    argumentsWithTrailingEmptyString match {
      case _ :: _ :: Nil =>
        renderCompletions(
          app.commands
            .filterNot(_.isHidden)
            .map(p => TabCompletionItem(p.subcommandName)),
          app
        )
      case _ :: subcommandName :: head :: tail =>
        app.commands.find(_.matchesName(subcommandName)).foreach { subcommand =>
          renderSubcommandCompletions(
            argumentLength,
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
      argumentLength: Int,
      shell: ShellCompletion,
      subcommand: CommandParser[_],
      head: String,
      tail: List[String],
      app: Application
  ): Unit = {
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
    val completionItems = tabCompletions(subcommand, context)
    renderCompletions(completionItems, app)
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
