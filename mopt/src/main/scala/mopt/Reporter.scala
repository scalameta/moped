package mopt

abstract class Reporter {
  def log(diag: Diagnostic): Unit
  def errorCount: Int
  def warningCount: Int
  def hasErrors: Boolean
  def hasWarnings: Boolean
  def reset(): Unit
}
