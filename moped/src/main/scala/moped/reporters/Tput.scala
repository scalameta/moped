package moped.reporters

import java.nio.file.Files
import java.nio.file.Paths

import scala.sys.process.ProcessLogger
import scala.util.Try
import scala.util.control.NonFatal

abstract class Tput {
  def size(): Option[ScreenSize]
}

object Tput {
  def constant(w: Int): Tput = () => Some(ScreenSize(w, w))
  def constant(w: Int, h: Int): Tput = () => Some(ScreenSize(w, h))
  val system: Tput =
    new Tput {
      def size(): Option[ScreenSize] = {
        Try {
          val tputPath =
            if (Files.exists(Paths.get("/usr/bin/tput")))
              "/usr/bin/tput"
            else
              "tput"
          try {
            val nullLog =
              new ProcessLogger {
                override def out(s: => String): Unit = ()
                override def err(s: => String): Unit = ()
                override def buffer[T](f: => T): T = f
              }
            val output =
              scala
                .sys
                .process
                .Process(
                  Seq(
                    "sh",
                    "-c",
                    s"""echo "cols\nlines" | $tputPath -S 2> /dev/tty"""
                  )
                )
                .!!(nullLog)
                .linesIterator
                .map(_.toInt)
                .toList
            output match {
              case cols :: lines :: Nil =>
                Some(ScreenSize(cols, lines))
              case _ =>
                None
            }
          } catch {
            case NonFatal(_) =>
              None
          }
        }.toOption.flatten
      }
    }
}
