package moped.generic

import moped.JsonMember
import moped.JsonObject
import moped.JsonElement
import moped.JsonEncoder
import moped.annotation.DeprecatedName
import scala.annotation.StaticAnnotation
import moped.annotation.DescriptionDoc
import org.typelevel.paiges.Doc
import moped.annotation.Description
import moped.annotation.Usage
import moped.annotation.ExampleUsage
import moped.internal.HelpMessage

final class Settings[T](
    val settings: List[Setting],
    val annotations: List[StaticAnnotation]
) {
  def this(settings: List[Setting]) = this(settings, Nil)
  def fields: List[Field] = settings.map(_.field)

  def flat(default: JsonObject): List[(Setting, JsonElement)] = {
    settings.zip(default.members).flatMap {
      case (deepSetting, JsonMember(_, conf: JsonObject)) =>
        deepSetting.underlying.toList
          .flatMap(_.withPrefix(deepSetting.name).flat(conf))
      case (s, JsonMember(_, defaultValue)) =>
        (s, defaultValue) :: Nil
    }
  }

  def withPrefix(prefix: String): Settings[T] =
    new Settings(settings.map(s => s.withName(prefix + "." + s.name)))

  override def toString: String = s"Surface(settings=$settings)"
  object Deprecated {
    def unapply(key: String): Option[DeprecatedName] =
      (for {
        setting <- settings
        deprecation <- setting.deprecation(key).toList
      } yield deprecation).headOption
  }
  def names: List[String] = settings.map(_.name)
  def allNames: List[String] =
    for {
      setting <- settings
      name <- setting.allNames
    } yield name

  def get(name: String): Option[Setting] =
    settings.find(_.matchesLowercase(name))

  def get(name: String, rest: List[String]): Option[Setting] =
    get(name).flatMap { setting =>
      if (setting.isDynamic) {
        Some(setting)
      } else {
        rest match {
          case Nil => Some(setting)
          case head :: tail =>
            for {
              underlying <- setting.underlying
              next <- underlying.get(head, tail)
            } yield next
        }
      }
    }

  def commandLineHelp(
      default: T
  )(implicit ev: JsonEncoder[T]): String =
    commandLineHelp(default, 80)
  def commandLineHelp(
      default: T,
      width: Int
  )(implicit ev: JsonEncoder[T]): String =
    HelpMessage.generate[T](default)(ev, this).renderTrim(width)

  def commandLineDescription: Option[Doc] =
    annotations.collectFirst {
      case DescriptionDoc(doc) => doc
      case Description(doc)    => Doc.text(doc)
    }
  def commandLineUsage: Option[Doc] =
    annotations.collectFirst {
      case Usage(doc) => Doc.text(doc)
    }
  def commandLineExamples: List[Doc] =
    annotations.collect {
      case ExampleUsage(example) => Doc.text(example)
    }

}

object Settings {
  implicit def FieldsToSettings[T](implicit ev: Surface[T]): Settings[T] =
    apply(ev)
  def apply[T](implicit ev: Surface[T]): Settings[T] =
    new Settings[T](ev.fields.flatten.map(new Setting(_)), ev.annotations)
}
