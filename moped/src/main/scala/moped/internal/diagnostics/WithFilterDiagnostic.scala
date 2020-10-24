package moped.internal.diagnostics

import moped.reporters._

class WithFilterDiagnostic[A](val value: A, val filter: A => Boolean)
    extends Diagnostic(
      ErrorSeverity,
      "WithFilterDiagnostic",
      NoPosition,
      Some(new NoSuchElementException())
    ) {
  def message: String = "Result.filter predicate is not satisfied"
}
