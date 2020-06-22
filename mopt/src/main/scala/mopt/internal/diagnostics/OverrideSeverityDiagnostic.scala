package mopt.internal.diagnostics

import mopt.Diagnostic
import mopt.Severity

class OverrideSeverityDiagnostic(val message: String, severity: Severity)
    extends Diagnostic(severity, "OverrideSeverity")
