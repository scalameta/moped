package moped.internal.diagnostics

import moped.reporters._

class WithFilterDiagnostic[A](value: A, filter: A => Boolean)
    extends Diagnostic(
      ErrorSeverity,
      "WithFilterDiagnostic",
      NoPosition,
      Some(new NoSuchElementException())
    ) {
  def message: String = "DecodingResult.filter predicate is not satisfied"
}
