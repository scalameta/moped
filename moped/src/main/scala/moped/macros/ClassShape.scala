package moped.macros

import scala.annotation.StaticAnnotation

import dataclass.data

/** Metadata about one class definition. */
@data
class ClassShape(
    name: String,
    fullyQualifiedName: String,
    parameters: List[List[ParameterShape]],
    annotations: List[StaticAnnotation]
)

object ClassShape {
  val empty: ClassShape = new ClassShape("", "", Nil, Nil)
}
