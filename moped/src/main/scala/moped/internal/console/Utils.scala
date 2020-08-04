package moped.internal.console

import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption

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
  def appendFile(path: Path, text: String): Unit = {
    Files.createDirectories(path.getParent())
    Files.write(
      path,
      text.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.APPEND,
      StandardOpenOption.TRUNCATE_EXISTING
    )
  }

}
