package moped.internal.diagnostics

import moped.reporters.Diagnostic
import moped.reporters.ErrorSeverity
import moped.reporters.NoPosition

class AggregateDiagnostic(head: Diagnostic, tail: List[Diagnostic])
    extends Diagnostic(ErrorSeverity, "", NoPosition, None, head :: tail) {
  def message: String = s"aggregate diagnostic $causes"
}
