package moped.internal.diagnostics

class MessageOnlyDiagnostic(val message: String)
    extends moped.Diagnostic(moped.ErrorSeverity)
