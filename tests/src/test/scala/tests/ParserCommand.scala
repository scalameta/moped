package tests

import moped.cli.Command
import moped.cli.Application
import moped.annotations.CommandName
import moped.cli.CommandParser

@CommandName("parser")
final case class ParserCommand(
    port: Int = 8080,
    longPort: Long = 8080,
    ratio: Double = 0.5,
    floatRatio: Float = 0.5f,
    app: Application = Application.default
) extends Command {
  def run(): Int = {
    if (port != ParserCommand.default.port)
      app.out.println(s"port=${port}")
    if (longPort != ParserCommand.default.longPort)
      app.out.println(s"longPort=${longPort}")
    if (ratio != ParserCommand.default.ratio)
      app.out.println(s"ratio=${ratio}")
    if (floatRatio != ParserCommand.default.floatRatio)
      app.out.println(s"floatRatio=${floatRatio}")
    0
  }
}

object ParserCommand {
  val default = ParserCommand()
  implicit val parser = CommandParser.derive(default)
}
