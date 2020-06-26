package mopt.internal.diagnostics

import mopt.Diagnostic
import mopt.ErrorSeverity
import mopt.NoPosition

class WithFilterDiagnostic[A](value: A, filter: A => Boolean)
    extends Diagnostic(
      ErrorSeverity,
      "WithFilterDiagnostic",
      NoPosition,
      Some(new NoSuchElementException())
    ) {
  def message: String = "DecodingResult.filter predicate is not satisfied"
}
