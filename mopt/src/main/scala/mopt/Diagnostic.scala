package mopt

import scala.collection.mutable
import mopt.internal.diagnostics.OverrideSeverityDiagnostic

abstract class Diagnostic(
    val severity: Severity,
    val id: String = "",
    val position: Position = NoPosition,
    val throwable: Option[Throwable] = None
) {
  private val severityOverrides = mutable.ListBuffer.empty[Diagnostic]

  def message: String

  def overrideSeverity(why: String, newSeverity: Severity): Unit = {
    severityOverrides += new OverrideSeverityDiagnostic(why, newSeverity)
  }
}
