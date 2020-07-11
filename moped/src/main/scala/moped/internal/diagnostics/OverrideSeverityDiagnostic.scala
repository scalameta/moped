package moped.internal.diagnostics

import moped.reporters.Diagnostic
import moped.reporters.Severity

class OverrideSeverityDiagnostic(val message: String, severity: Severity)
    extends Diagnostic(severity, "OverrideSeverity")
