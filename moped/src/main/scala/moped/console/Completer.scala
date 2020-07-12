package moped.console

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File

trait Completer[A] {
  def complete(context: TabCompletionContext): List[TabCompletionItem]
}

object Completer {

  implicit def completerToIterableCompleter[A, C[x] <: Iterable[x]](implicit
      ev: Completer[A]
  ): Completer[C[Completer[A]]] =
    context => ev.complete(context)
  implicit def completerToOptionCompleter[A](implicit
      ev: Completer[A]
  ): Completer[Option[Completer[A]]] =
    context => ev.complete(context)

  implicit lazy val pathCompleter: Completer[Path] = { context =>
    val pathOrDirectory = Paths.get(context.last)
    val absolutePathOrDirectory =
      if (pathOrDirectory.isAbsolute()) pathOrDirectory
      else context.app.env.workingDirectory.resolve(pathOrDirectory)
    val path: Path =
      if (context.last.endsWith(File.separator)) {
        absolutePathOrDirectory
      } else if (context.last.isEmpty()) {
        absolutePathOrDirectory
      } else {
        Option(absolutePathOrDirectory.getParent())
          .getOrElse(absolutePathOrDirectory)
      }
    if (Files.isDirectory(path)) {
      path
        .toFile()
        .listFiles()
        .iterator
        .map(_.toPath())
        .map { p =>
          val slash = if (Files.isDirectory(p)) File.separator else ""
          val prefix =
            if (pathOrDirectory.isAbsolute()) {
              p.toString()
            } else {
              context.app.env.workingDirectory.relativize(p).toString()
            }
          prefix + slash
        }
        .map(TabCompletionItem(_))
        .toList
        .sortBy(_.name)
    } else {
      Nil
    }
  }
}
