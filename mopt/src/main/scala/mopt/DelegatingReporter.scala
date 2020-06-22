package mopt

class DelegatingReporter(val underlying: Reporter) extends Reporter {
  override def log(message: Diagnostic): Unit = underlying.log(message)
  override def errorCount: Int = underlying.errorCount
  override def warningCount: Int = underlying.warningCount
  override def hasErrors: Boolean = underlying.hasErrors
  override def hasWarnings: Boolean = underlying.hasWarnings
  override def reset(): Unit = underlying.reset()
}
