package moped.internal.diagnostics

import moped.Diagnostic
import moped.ErrorSeverity
import moped.NoPosition

class WithFilterDiagnostic[A](value: A, filter: A => Boolean)
    extends Diagnostic(
      ErrorSeverity,
      "WithFilterDiagnostic",
      NoPosition,
      Some(new NoSuchElementException())
    ) {
  def message: String = "DecodingResult.filter predicate is not satisfied"
}
