package moped.internal.console

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
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
    if (!Files.isRegularFile(path))
      return
    val before = Files.readAllLines(path).asScala
    val after = before.filterNot(_.contains(query))
    if (!before.sameElements(after)) {
      Files.write(
        path,
        after.asJava,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
    }
  }

  def readInputStreamString(is: InputStream): String = {
    new String(readInputStreamBytes(is), StandardCharsets.UTF_8)
  }
  def readInputStreamBytes(is: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val buffer = new Array[Byte](4096)
    var nread = -1
    do {
      nread = is.read(buffer, 0, buffer.length)
      if (nread != -1)
        baos.write(buffer, 0, nread)
    } while (nread != -1)
    baos.toByteArray
  }

}
