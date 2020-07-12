package moped.generic

import scala.annotation.StaticAnnotation
import moped.annotations._
import moped.internal.console.CommandLineParser

/**
 * Metadata about one field of a class.
 *
 * @param name the parameter name of this field.
 * @param tpe the pretty-printed type of this parameter
 * @param annotations static annotations attached to this field.
 */
final class ParameterShape(
    val name: String,
    val tpe: String,
    val annotations: List[StaticAnnotation],
    val underlying: Option[ClassShaper[_]]
) {
  def withName(newName: String) =
    new ParameterShape(newName, tpe, annotations, underlying)

  /**
   * Returns this field with all underlying fields expaneded.
   *
   * Underlying field names become prefixed by their enclosing fields.
   */
  def flat: List[ParameterShape] = {
    if (underlying.isEmpty) this :: Nil
    else {
      this :: (for {
        cls <- underlying.toList
        param <- cls.fields
        flattened <- field.withName(s"$name.${field.name}").flat
      } yield flattened)
    }
  }
  override def toString: String = {
    val annots = annotations.map(annot => s"@$annot").mkString(", ")
    s"""ClassParameter(name="$name",tpe="$tpe",annotations=List($annots),underlying=$underlying)"""
  }

  def field = this

  def description: Option[String] =
    annotations.collectFirst {
      case Description(value) => value
    }
  def extraNames: List[String] =
    field.annotations.collect {
      case PositionalArguments() => CommandLineParser.PositionalArgument
      case ExtraName(value)      => value
    }
  def deprecatedNames: List[DeprecatedName] =
    field.annotations.collect {
      case d: DeprecatedName => d
    }
  def exampleValues: List[String] =
    field.annotations.collect {
      case ExampleValue(value) => value
    }
  def sinceVersion: Option[String] =
    field.annotations.collectFirst {
      case SinceVersion(value) => value
    }
  def deprecated: Option[Deprecated] =
    field.annotations.collectFirst {
      case value: Deprecated => value
    }
  def isPositionalArguments: Boolean =
    field.annotations.exists(_.isInstanceOf[PositionalArguments])
  def isRepeated: Boolean =
    field.annotations.exists(_.isInstanceOf[Repeated])
  def isDynamic: Boolean =
    annotations.exists(_.isInstanceOf[Dynamic])
  def isHidden: Boolean =
    annotations.exists(_.isInstanceOf[Hidden])
  def isBoolean: Boolean =
    annotations.exists(_.isInstanceOf[Flag])
  def isTabCompleteAsPath: Boolean =
    annotations.exists(_.isInstanceOf[TabCompleteAsPath])
  def isCatchInvalidFlags: Boolean =
    annotations.exists(_.isInstanceOf[CatchInvalidFlags])
  def isPositionalArgument: Boolean =
    annotations.exists {
      case ExampleValue(CommandLineParser.PositionalArgument) => true
      case _                                                  => false
    }
  def tabCompleteOneOf: Option[List[String]] =
    annotations.collectFirst {
      case oneof: TabCompleteAsOneOf => oneof.options.toList
    }
  def alternativeNames: List[String] =
    extraNames ::: deprecatedNames.map(_.name)
  def allNames: List[String] = name :: alternativeNames
  def matchesLowercase(name: String): Boolean =
    allNames.exists(_.equalsIgnoreCase(name))
  def deprecation(name: String): Option[DeprecatedName] =
    deprecatedNames.find(_.name == name)
}
