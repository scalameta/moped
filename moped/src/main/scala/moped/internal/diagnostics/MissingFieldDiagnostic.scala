package moped.internal.diagnostics

import moped.json.DecodingContext
import moped.reporters.Diagnostic
import moped.reporters.ErrorSeverity

class MissingFieldDiagnostic(context: DecodingContext)
    extends Diagnostic(ErrorSeverity, "MissingField", context.json.position) {
  def message: String =
    s"${context.cursor.path} is not a member of ${context.json}"
}
