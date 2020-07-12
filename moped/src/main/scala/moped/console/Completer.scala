package moped.console

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File
import moped.internal.console.PathCompleter

trait Completer[A] {
  def complete(context: TabCompletionContext): List[TabCompletionItem]
}

object Completer {
  implicit val pathCompleter: Completer[Path] = PathCompleter

  implicit def iterableCompleter[A, C[x] <: Iterable[x]](implicit
      ev: Completer[A]
  ): Completer[C[A]] = ev.asInstanceOf[Completer[C[A]]]
  implicit def optionCompleter[A](implicit
      ev: Completer[A]
  ): Completer[Option[A]] = ev.asInstanceOf[Completer[Option[A]]]

}
