package moped.internal

import moped._
import moped.internal.Cases
import moped.generic.Setting
import moped.generic.Settings
import moped.annotation.Inline
import CommandLineParser._

class CommandLineParser[T](
    args: List[String],
    settings: Settings[T],
    toInline: Map[String, Setting]
) {
  def loop(
      curr: JsonObject,
      xs: List[String],
      s: State
  ): DecodingResult[JsonObject] = {
    (xs, s) match {
      case (Nil, NoFlag) => ValueResult(curr)
      case (Nil, Flag(flag, setting)) =>
        if (setting.isBoolean) ValueResult(add(curr, flag, JsonBoolean(true)))
        else {
          ErrorResult(
            Diagnostic.message(
              s"the argument '--${Cases.camelToKebab(flag)}' requires a value but none was supplied"
            )
          )
        }
      case (head :: tail, NoFlag) =>
        val equal = head.indexOf('=')
        if (equal >= 0) { // split "--key=value" into ["--key", "value"]
          val key = head.substring(0, equal)
          val value = head.substring(equal + 1)
          loop(curr, key :: value :: tail, NoFlag)
        } else if (head.startsWith("-")) {
          tryFlag(curr, head, tail, s, defaultBooleanValue = true) match {
            case nok: ErrorResult if head.startsWith("--") =>
              val fallbackFlag =
                if (head.startsWith(noPrefix)) {
                  "--" + head.stripPrefix(noPrefix)
                } else {
                  noPrefix + head.stripPrefix("--")
                }
              val fallback =
                tryFlag(
                  curr,
                  fallbackFlag,
                  tail,
                  s,
                  defaultBooleanValue = false
                )
              fallback.orElse(nok)
            case ok => ok
          }
        } else {
          val positionalArgs =
            appendValues(
              curr,
              PositionalArgument,
              List(JsonString(head))
            )
          loop(add(curr, PositionalArgument, positionalArgs), tail, NoFlag)
        }
      case (head :: tail, Flag(flag, setting)) =>
        val value = JsonString(head)
        val newCurr =
          if (setting.isRepeated) {
            appendValues(curr, flag, List(value))
          } else {
            value
          }
        loop(add(curr, flag, newCurr), tail, NoFlag)
    }
  }

  private def tryFlag(
      curr: JsonObject,
      head: String,
      tail: List[String],
      s: State,
      defaultBooleanValue: Boolean
  ): DecodingResult[JsonObject] = {
    val camel = Cases.kebabToCamel(dash.replaceFirstIn(head, ""))
    camel.split("\\.").toList match {
      case Nil =>
        ErrorResult(Diagnostic.message(s"Flag '$head' must not be empty"))
      case flag :: flags =>
        val (key, keys) = toInline.get(flag) match {
          case Some(setting) => setting.name -> (flag :: flags)
          case _             => flag -> flags
        }
        settings.get(key, keys) match {
          case None =>
            settings.settings.find(_.isCatchInvalidFlags) match {
              case None =>
                val closestCandidate =
                  Levenshtein.closestCandidate(camel, settings.names)
                val didYouMean = closestCandidate match {
                  case None =>
                    ""
                  case Some(candidate) =>
                    val kebab = Cases.camelToKebab(candidate)
                    s"\n\tDid you mean '--$kebab'?"
                }
                ErrorResult(
                  Diagnostic.message(
                    s"found argument '--$flag' which wasn't expected, or isn't valid in this context.$didYouMean"
                  )
                )
              case Some(fallback) =>
                val values = appendValues(
                  curr,
                  PositionalArgument,
                  (head :: tail).map(JsonString(_))
                )
                ValueResult(add(curr, PositionalArgument, values))
            }
          case Some(setting) =>
            val prefix = toInline.get(flag).fold("")(_.name + ".")
            val toAdd = prefix + camel
            if (setting.isBoolean) {
              val newCurr =
                add(curr, toAdd, JsonBoolean(defaultBooleanValue))
              loop(newCurr, tail, NoFlag)
            } else {
              loop(curr, tail, Flag(toAdd, setting))
            }
        }
    }
  }
}

object CommandLineParser {
  val PositionalArgument = "remainingArgs"

  def parseArgs[T](
      args: List[String]
  )(implicit settings: Settings[T]): DecodingResult[JsonElement] = {
    val toInline = inlinedSettings(settings)
    val parser = new CommandLineParser[T](args, settings, toInline)
    parser.loop(JsonObject(Nil), args, NoFlag).map(_.normalize)
  }

  private def add(
      curr: JsonObject,
      key: String,
      value: JsonElement
  ): JsonObject = {
    val values = curr.members.filterNot {
      case JsonMember(k, _) => k.value == key
    }
    JsonObject(JsonMember(JsonString(key), value) :: values)
  }

  val noPrefix = "--no-"
  def isNegatedBoolean(flag: String): Boolean = flag.startsWith(noPrefix)

  def appendValues(
      obj: JsonObject,
      key: String,
      values: List[JsonElement]
  ): JsonArray = {
    obj.getMember(key) match {
      case Some(JsonArray(oldValues)) => JsonArray(oldValues ++ values)
      case _                          => JsonArray(values)
    }
  }

  def inlinedSettings(settings: Settings[_]): Map[String, Setting] =
    settings.settings.iterator.flatMap { setting =>
      if (setting.annotations.exists(_.isInstanceOf[Inline])) {
        for {
          underlying <- setting.underlying.toList
          name <- underlying.names
        } yield name -> setting
      } else {
        Nil
      }
    }.toMap

  def allSettings(settings: Settings[_]): Map[String, Setting] =
    inlinedSettings(settings) ++ settings.settings.map(s => s.name -> s)

  private sealed trait State
  private case class Flag(flag: String, setting: Setting) extends State
  private case object NoFlag extends State
  private val dash = "--?".r

}
