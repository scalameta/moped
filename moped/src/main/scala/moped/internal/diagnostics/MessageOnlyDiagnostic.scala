package moped.internal.diagnostics

import dataclass.data
import moped.reporters.NoPosition
import moped.reporters.Position
import moped.reporters.Severity

@data
class MessageOnlyDiagnostic(
    override val message: String,
    override val severity: Severity,
    @since
    override val position: Position = NoPosition,
    @since
    override val throwable: Option[Throwable] = None
) extends moped.reporters.Diagnostic(
      severity,
      position = position,
      throwable = throwable
    )
