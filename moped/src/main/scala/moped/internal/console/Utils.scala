package moped.internal.console

import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import scala.collection.JavaConverters._

object Utils {
  def readFile(path: Path): String = {
    new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
  }
  def overwriteFile(path: Path, text: String): Unit = {
    Files.createDirectories(path.getParent())
    Files.write(
      path,
      text.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
  }
  def appendLines(path: Path, text: List[String]): Unit = {
    Files.createDirectories(path.getParent())
    Files.write(
      path,
      text.asJava,
      StandardOpenOption.CREATE,
      StandardOpenOption.APPEND
    )

  }
  def filterLinesMatching(path: Path, query: String): Unit = {
    val before = Utils.readFile(path)
    val after = before.linesIterator.filterNot(_.contains(query)).mkString("\n")
    if (before != after) {
      Utils.overwriteFile(path, after)
    }
  }

}
