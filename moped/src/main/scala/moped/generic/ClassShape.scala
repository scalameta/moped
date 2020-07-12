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

object ClassShape {
  def apply[T](
      f: List[List[ParameterShape]]
  ): ClassShape[T] = apply[T](f, Nil)
  def apply[T](
      f: List[List[ParameterShape]],
      a: List[StaticAnnotation]
  ): ClassShape[T] =
    new ClassShape[T] {
      def fields: List[List[ParameterShape]] = f
      def annotations: List[StaticAnnotation] = a
    }
}

trait ClassShape[T] {
  def fields: List[List[ParameterShape]]
  def annotations: List[StaticAnnotation]

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

  def get(name: String): Option[ParameterShape] =
    settings.find(_.matchesLowercase(name))

  def get(name: String, rest: List[String]): Option[ParameterShape] =
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
