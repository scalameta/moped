package moped.generic

import moped.json._
import scala.annotation.StaticAnnotation
import moped.annotations.DescriptionDoc
import org.typelevel.paiges.Doc
import moped.annotations.Description
import moped.annotations.Usage
import moped.annotations.ExampleUsage
import moped.internal.console.HelpMessage

object ClassShaper {
  def empty[T]: ClassShaper[T] =
    ClassShaper[T](ClassShape.empty)
  def apply[T](s: ClassShape): ClassShaper[T] =
    new ClassShaper[T] { def shape: ClassShape = s }
}

trait ClassShaper[T] {
  def shape: ClassShape

  def parameters = shape.parameters
  def parametersFlat = parameters.flatten

  def annotations = shape.annotations

  override def toString: String = s"ClassShaper($shape)"
  def names: List[String] = parametersFlat.map(_.name)
  def allNames: List[String] =
    for {
      setting <- parametersFlat
      name <- setting.allNames
    } yield name

  def get(name: String): Option[ParameterShape] =
    parametersFlat.find(_.matchesLowercase(name))

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
