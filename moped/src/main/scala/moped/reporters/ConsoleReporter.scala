package moped.reporters

import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger

import fansi.Attrs

class ConsoleReporter(
    ps: PrintStream,
    severityColor: Severity => Attrs = sev => Severity.color(sev)
) extends Reporter {

  private val counts: Map[Severity, AtomicInteger] =
    Severity.all.map(s => s -> new AtomicInteger).toMap

  override def log(diag: Diagnostic): Unit = {
    val prefix = severityColor(diag.severity)
      .apply(diag.severity.name + ": ")
      .toString()
    counts(diag.severity).incrementAndGet()
    ps.println(prefix + diag.message)
  }

  override def errorCount(): Int = counts(ErrorSeverity).get()
  override def warningCount(): Int = counts(WarningSeverity).get()
  override def reset(): Unit = counts.valuesIterator.foreach(_.set(0))

}
