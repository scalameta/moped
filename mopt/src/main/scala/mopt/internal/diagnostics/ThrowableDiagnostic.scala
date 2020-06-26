package mopt.internal.diagnostics

import mopt.Diagnostic
import mopt.Position
import mopt.ErrorSeverity
import mopt.NoPosition

class ThrowableDiagnostic(error: Throwable, pos: Position)
    extends Diagnostic(
      ErrorSeverity,
      error.getClass().getName(),
      pos,
      Some(error)
    ) {
  def this(error: Throwable) = this(error, NoPosition)
  def message: String = error.getMessage()
}
