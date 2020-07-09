package moped

object NoReporter extends Reporter {
  override def log(message: Diagnostic): Unit = ()
  override def errorCount(): Int = 0
  override def warningCount(): Int = 0
  override def reset(): Unit = ()
}
