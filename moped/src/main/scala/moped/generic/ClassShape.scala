package moped.generic

import scala.annotation.StaticAnnotation

/** Metadata about one class definition. */
final class ClassShape(
    val name: String,
    val fullyQualifiedName: String,
    val parameters: List[List[ParameterShape]],
    val annotations: List[StaticAnnotation]
) {
  override def toString(): String = {
    val prettyAnnotations = annotations.map(annot => s"@$annot").mkString(", ")
    s"""ClassShape(name="$name",fullyQualifiedName="$fullyQualifiedName",parameters=$parameters,annotations=List($prettyAnnotations))"""
  }
}

object ClassShape {
  val empty: ClassShape = new ClassShape("", "", Nil, Nil)
}
