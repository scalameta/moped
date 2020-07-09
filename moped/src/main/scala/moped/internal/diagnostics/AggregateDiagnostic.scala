package moped.internal.diagnostics

import moped.Diagnostic
import moped.ErrorSeverity
import moped.NoPosition

class AggregateDiagnostic(head: Diagnostic, tail: List[Diagnostic])
    extends Diagnostic(ErrorSeverity, "", NoPosition, None, head :: tail) {
  def message: String = s"aggregate diagnostic $causes"
}
