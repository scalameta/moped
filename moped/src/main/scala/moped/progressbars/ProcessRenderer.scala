package moped.progressbars

import java.time.Duration
import java.util.concurrent.ConcurrentLinkedDeque

import scala.collection.JavaConverters._
import scala.math.Ordered._

import fansi.ErrorMode
import fansi.Str
import moped.internal.reporters.Docs
import moped.json.ErrorResult
import moped.reporters.Diagnostic
import org.typelevel.paiges.Doc
import os.ProcessOutput

class ProcessRenderer(
    command: List[String],
    prettyCommand: List[String],
    onStop: PrettyTimer => Doc = _ => Doc.empty,
    minimumDuration: Duration = Duration.ofSeconds(1)
) extends ProgressRenderer {
  val lines = new ConcurrentLinkedDeque[String]()
  val output: ProcessOutput.Readlines = os
    .ProcessOutput
    .Readlines(line => lines.addLast(line))
  lazy val timer = new PrettyTimer
  private def prettyPrintCommand(c: List[String]) =
    c.map(arg =>
        if (arg.contains(" "))
          s"'$arg'"
        else
          arg
      )
      .mkString(" ")
  val commandString: String = prettyPrintCommand(command)
  val prettyCommandString: String = prettyPrintCommand(prettyCommand)
  private def shortCommandString =
    if (commandString.length() > 60)
      commandString.take(60) + "..."
    else
      commandString
  def asErrorResult(exitCode: Int): ErrorResult = {
    lines.addFirst(
      s"Command failed with exit code '${exitCode}', to reproduce:\n  $$ ${commandString}"
    )
    ErrorResult(Diagnostic.error(lines.asScala.mkString("\n")))
  }
  override def renderStop(): Doc = {
    if (timer.elapsed > minimumDuration) {
      Docs.successMessage(s"Ran '$prettyCommandString' in $timer")
    } else {
      Doc.empty
    }
  }
  override def renderStep(): ProgressStep = {
    val elapsed = timer.elapsed()
    val lastLine = lines.peekLast()
    if (elapsed > minimumDuration && lastLine != null) {
      // Strip out ANSI escape codes since they mess up with the progress bar.
      val lastLineClean = Str(lastLine, errorMode = ErrorMode.Strip).plainText
      val endLine =
        if (lastLineClean.isEmpty())
          Doc.empty
        else
          Doc.line
      val dynamic =
        Doc.text(timer.formatPadded()) + Doc.space +
          Doc.text(prettyCommandString) + Doc.line + Doc.text(lastLineClean) +
          endLine
      ProgressStep(dynamic = dynamic)
    } else {
      ProgressStep.empty
    }
  }
}
