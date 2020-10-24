package moped.reporters

import moped.json.Result
import moped.json.ErrorResult
import moped.json.ValueResult

abstract class Reporter {
  def log(diag: Diagnostic): Unit
  final def debug(message: String): Unit = log(Diagnostic.debug(message))
  final def info(message: String): Unit = log(Diagnostic.info(message))
  final def warning(message: String): Unit = log(Diagnostic.warning(message))
  final def error(message: String): Unit = log(Diagnostic.error(message))
  def errorCount(): Int
  def warningCount(): Int
  def reset(): Unit

  final def exit(result: Result[Unit]): Int = {
    result match {
      case ValueResult(()) =>
        exitCode()
      case ErrorResult(error) =>
        log(error)
        1
    }
  }
  final def exitCode(): Int =
    if (hasErrors())
      1
    else
      0
  final def hasErrors(): Boolean = errorCount > 0
  final def hasWarnings(): Boolean = warningCount > 0
}
