package moped.internal.diagnostics

import moped.reporters._

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
