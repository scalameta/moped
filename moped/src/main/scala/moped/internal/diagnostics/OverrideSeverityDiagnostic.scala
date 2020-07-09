package moped.internal.diagnostics

import moped.Diagnostic
import moped.Severity

class OverrideSeverityDiagnostic(val message: String, severity: Severity)
    extends Diagnostic(severity, "OverrideSeverity")
