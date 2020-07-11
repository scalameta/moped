package moped.generic

import moped.json._
import moped.annotations.DeprecatedName
import scala.annotation.StaticAnnotation
import moped.annotations.DescriptionDoc
import org.typelevel.paiges.Doc
import moped.annotations.Description
import moped.annotations.Usage
import moped.annotations.ExampleUsage
import moped.internal.console.HelpMessage

final class ClassDefinition[T](
    val fields: List[List[ClassParameter]],
    val annotations: List[StaticAnnotation]
) {
  def settings = fields.flatten

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

  def get(name: String): Option[ClassParameter] =
    settings.find(_.matchesLowercase(name))

  def get(name: String, rest: List[String]): Option[ClassParameter] =
    get(name).flatMap { setting =>
      if (setting.isDynamic) {
        Some(setting)
      } else {
        rest match {
          case Nil => Some(setting)
          case head :: tail =>
            setting.underlying.iterator.flatten
              .map(_.get(head, tail))
              .collectFirst {
                case Some(value) => value
              }
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
