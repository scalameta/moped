package moped.reporters

abstract class Reporter {
  def log(diag: Diagnostic): Unit
  def errorCount(): Int
  def warningCount(): Int
  def reset(): Unit

  final def hasErrors(): Boolean = errorCount > 0
  final def hasWarnings(): Boolean = warningCount > 0
}
