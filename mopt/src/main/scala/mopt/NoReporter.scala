package mopt

object NoReporter extends Reporter {
  override def log(message: Diagnostic): Unit = ()
  override def errorCount: Int = 0
  override def warningCount: Int = 0
  override def hasErrors: Boolean = false
  override def hasWarnings: Boolean = false
  override def reset(): Unit = ()
}
