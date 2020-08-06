package moped.internal.diagnostics

import moped.reporters.Severity

class MessageOnlyDiagnostic(val message: String, severity: Severity)
    extends moped.reporters.Diagnostic(severity)
