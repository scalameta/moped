package moped.macros

import scala.annotation.StaticAnnotation

/** Metadata about one class definition. */
final case class ClassShape(
    val name: String,
    val fullyQualifiedName: String,
    val parameters: List[List[ParameterShape]],
    val annotations: List[StaticAnnotation]
)

object ClassShape {
  val empty: ClassShape = new ClassShape("", "", Nil, Nil)
}
