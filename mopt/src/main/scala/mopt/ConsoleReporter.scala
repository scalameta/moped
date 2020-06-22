package mopt

import java.io.PrintStream
import fansi.Attrs
import fansi.Color
import fansi.Str
import fansi.Attr

class ConsoleReporter(
    ps: PrintStream,
    severityColor: Severity => Attrs = sev => Severity.color(sev)
) extends Reporter {

  private var myWarnings = 0
  private var myErrors = 0
  val colon = Str(": ")
  val counts = Map()

  override def log(diag: Diagnostic): Unit = {
    val prefix = severityColor(diag.severity)
      .apply(diag.severity.name + ": ")
      .toString()
    ps.print(prefix + diag.message)
  }

  override def errorCount: Int = ???

  override def warningCount: Int = ???

  override def hasErrors: Boolean = ???

  override def hasWarnings: Boolean = ???

  override def reset(): Unit = ???

}
