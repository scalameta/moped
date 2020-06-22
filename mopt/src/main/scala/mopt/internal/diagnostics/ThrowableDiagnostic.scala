package mopt.internal.diagnostics

import mopt.Diagnostic
import mopt.Position
import mopt.ErrorSeverity

class ThrowableDiagnostic(error: Throwable, pos: Position)
    extends Diagnostic(
      ErrorSeverity,
      error.getClass().getName(),
      pos,
      Some(error)
    ) {
  def message: String = error.getMessage()
}
