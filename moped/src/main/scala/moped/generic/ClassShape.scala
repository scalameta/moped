package moped.generic

import scala.annotation.StaticAnnotation

final class ClassShape(
    val name: String,
    val fullyQualifiedName: String,
    val fields: List[List[ParameterShape]],
    val annotations: List[StaticAnnotation]
)
