package mopt.internal.diagnostics

import mopt.Diagnostic
import mopt.ErrorSeverity
import mopt.NoPosition

class AggregateDiagnostic(head: Diagnostic, tail: List[Diagnostic])
    extends Diagnostic(ErrorSeverity, "", NoPosition, None, head :: tail) {
  def message: String = s"aggregate diagnostic $causes"
}
