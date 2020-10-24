package moped.internal.diagnostics

import moped.reporters.NoPosition
import moped.reporters.Position
import moped.reporters.Severity

case class MessageOnlyDiagnostic(
    override val message: String,
    override val severity: Severity,
    override val position: Position = NoPosition,
    override val throwable: Option[Throwable] = None
) extends moped.reporters.Diagnostic(
      severity,
      position = position,
      throwable = throwable
    )
