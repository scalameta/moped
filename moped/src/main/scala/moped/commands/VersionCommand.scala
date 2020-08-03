package moped.commands

import moped.annotations.CatchInvalidFlags
import moped.annotations.Description
import moped.annotations.Hidden
import moped.annotations.PositionalArguments
import moped.console.CodecCommandParser
import moped.console.CommandParser
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.macros.ClassShape
import moped.macros.ClassShaper
import moped.macros.ParameterShape
import moped.console.Command
import moped.console.Application
import moped.annotations.CommandName

object VersionCommand {
  val default = new VersionCommand()

  implicit lazy val parser: CommandParser[VersionCommand] =
    new CodecCommandParser[VersionCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(
          new ClassShape(
            "VersionCommand",
            "moped.commands.VersionCommand",
            List(),
            List(
              Description("Print the version of this program"),
              CommandName("version")
            )
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[VersionCommand](_ => ""),
        JsonDecoder.constant(default)
      ),
      default
    )
}

class VersionCommand extends Command {
  override def run(app: Application): Int = {
    app.out.println(app.version)
    0
  }
}
