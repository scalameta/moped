package moped

class DelegatingReporter(val underlying: Reporter) extends Reporter {
  override def log(message: Diagnostic): Unit = underlying.log(message)
  override def errorCount(): Int = underlying.errorCount
  override def warningCount(): Int = underlying.warningCount
  override def reset(): Unit = underlying.reset()
}
