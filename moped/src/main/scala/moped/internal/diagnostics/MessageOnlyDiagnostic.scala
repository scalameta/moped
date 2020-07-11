package moped.internal.diagnostics

class MessageOnlyDiagnostic(val message: String)
    extends moped.reporters.Diagnostic(moped.reporters.ErrorSeverity)
