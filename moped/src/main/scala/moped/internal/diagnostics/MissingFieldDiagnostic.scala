package moped.internal.diagnostics

import moped.json.JsonElement
import moped.json.Cursor
import moped.reporters.Diagnostic
import moped.reporters.ErrorSeverity
import moped.json.DecodingContext

class MissingFieldDiagnostic(context: DecodingContext)
    extends Diagnostic(ErrorSeverity, "MissingField", context.json.position) {
  def message: String =
    s"${context.cursor.path} is not a member of ${context.json}"
}
