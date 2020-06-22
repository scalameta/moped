package mopt

import scala.collection.mutable
import mopt.internal.diagnostics.OverrideSeverityDiagnostic
import mopt.internal.diagnostics.AggregateDiagnostic

abstract class Diagnostic(
    val severity: Severity,
    val id: String = "",
    val position: Position = NoPosition,
    val throwable: Option[Throwable] = None,
    val causes: List[Diagnostic] = Nil
) {
  private val severityOverrides = mutable.ListBuffer.empty[Diagnostic]

  def message: String

  def overrideSeverity(why: String, newSeverity: Severity): Unit = {
    severityOverrides += new OverrideSeverityDiagnostic(why, newSeverity)
  }
}

object Diagnostic {
  def fromDiagnostics(diagnostics: List[Diagnostic]): Option[Diagnostic] = {
    diagnostics match {
      case Nil          => None
      case head :: Nil  => Some(head)
      case head :: tail => Some(new AggregateDiagnostic(head, tail))
    }
  }
}
