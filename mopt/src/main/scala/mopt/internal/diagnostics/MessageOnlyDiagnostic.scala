package mopt.internal.diagnostics

class MessageOnlyDiagnostic(val message: String)
    extends mopt.Diagnostic(mopt.ErrorSeverity)
