package moped.internal.diagnostics

import scala.collection.immutable.Nil

import moped.reporters.Diagnostic
import moped.reporters.ErrorSeverity
import moped.reporters.NoPosition

class AggregateDiagnostic(head: Diagnostic, tail: List[Diagnostic])
    extends Diagnostic(ErrorSeverity, "", NoPosition, None, head :: tail) {
  def message: String =
    tail match {
      case Nil =>
        head.pretty
      case _ =>
        val all = head :: tail
        val count = all.size
        val summary = s"\n$count errors"
        all
          .zipWithIndex
          .map { case (d, i) =>
            s"[E${i + 1}] ${d.pretty}"
          }
          .mkString("", "\n", summary)
    }
}
