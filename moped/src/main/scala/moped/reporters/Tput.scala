package moped.reporters

import java.nio.file.Files
import java.nio.file.Paths

import scala.sys.process.Process
import scala.util.control.NonFatal

abstract class Tput {
  def cols(): Option[Int]
}

object Tput {
  def constant(n: Int): Tput = () => Some(n)
  val system: Tput = () => {
    val pathedTput =
      if (Files.exists(Paths.get("/usr/bin/tput"))) "/usr/bin/tput"
      else "tput"
    try {
      val columns = Process(
        Seq("sh", "-c", s"$pathedTput cols 2> /dev/tty")
      ).!!.trim.toInt
      Some(columns.toInt)
    } catch {
      case NonFatal(_) =>
        None
    }

  }
}
