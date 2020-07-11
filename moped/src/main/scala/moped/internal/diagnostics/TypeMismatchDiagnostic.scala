package moped.internal.diagnostics

import moped.reporters._
import moped.json._

class TypeMismatchDiagnostic(
    expected: String,
    obtained: String,
    pos: Position,
    cursor: Cursor
) extends Diagnostic(ErrorSeverity, "TypeMismatch", pos) {
  def this(expected: String, context: DecodingContext) =
    this(
      expected,
      context.json.productPrefix,
      context.json.position,
      context.cursor
    )
  override def message: String = {
    val pathSuffix = if (cursor.isEmpty) "" else s"at ${cursor.path}"
    s"""Type mismatch$pathSuffix;
       |  found    : $obtained
       |  expected : $expected""".stripMargin
  }
}
