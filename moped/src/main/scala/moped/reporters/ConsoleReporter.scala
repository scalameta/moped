package moped.reporters

import java.io.PrintStream
import java.util.concurrent.atomic.AtomicInteger

import fansi.Attrs
import moped.cli.Environment

class ConsoleReporter private (
    ps: PrintStream,
    severityColor: Severity => Attrs
) extends Reporter {

  private val counts: Map[Severity, AtomicInteger] =
    Severity.all.map(s => s -> new AtomicInteger).toMap

  override def log(diag: Diagnostic): Unit = {
    val severity =
      severityColor(diag.severity).apply(diag.severity.name).toString()
    counts(diag.severity).incrementAndGet()
    ps.println(diag.position.pretty(severity, diag.message))
  }

  override def errorCount(): Int = counts(ErrorSeverity).get()
  override def warningCount(): Int = counts(WarningSeverity).get()
  override def reset(): Unit = counts.valuesIterator.foreach(_.set(0))

}

object ConsoleReporter {
  def apply(ps: PrintStream): ConsoleReporter = {
    ConsoleReporter(ps, Environment.default.isColorEnabled)
  }
  def apply(ps: PrintStream, isColorEnabled: Boolean): ConsoleReporter = {
    new ConsoleReporter(
      ps,
      severityColor = sev =>
        if (isColorEnabled) Severity.color(sev)
        else Attrs.Empty
    )
  }
}
