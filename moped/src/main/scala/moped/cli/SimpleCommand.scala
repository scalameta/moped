package moped.cli

import scala.util.control.NonFatal

import moped.internal.console.StackTraces

class SimpleCommand(app: Application, fn: Application => Int) extends Command {
  def run(): Int = {
    try {
      val args =
        app.arguments match {
          case app.binaryName :: tail =>
            tail
          case other =>
            other
        }
      StackTraces.dropOutside {
        fn(app.copy(arguments = args))
      }
    } catch {
      case NonFatal(e) =>
        app.printStackTrace(e)
        1
    }
  }
}
