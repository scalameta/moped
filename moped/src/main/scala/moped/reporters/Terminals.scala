package moped.reporters

import java.nio.file.Files
import java.nio.file.Paths

import scala.sys.process.Process
import scala.util.control.NonFatal

object Terminals {
  def isColorEnabled: Boolean =
    "true" == System.getenv("NO_COLOR")
  def screenWidth(): Int = {
    screenWidth(lowerBound = 40, upperBound = 100)
  }
  def screenWidth(lowerBound: Int, upperBound: Int): Int = {
    math.min(upperBound, math.max(lowerBound, tputsColumns() - 20))
  }
  private def tputsColumns(fallback: Int = 80): Int = {
    val pathedTput =
      if (Files.exists(Paths.get("/usr/bin/tput"))) "/usr/bin/tput"
      else "tput"
    try {
      val columns = Process(
        Seq("sh", "-c", s"$pathedTput cols 2> /dev/tty")
      ).!!.trim.toInt
      columns.toInt
    } catch {
      case NonFatal(_) =>
        fallback
    }
  }
}
