package moped.console

import moped.annotations.CommandName
import moped.annotations.Description
import moped.annotations.Hidden
import moped.json.JsonCodec
import moped.json.JsonDecoder
import moped.json.JsonEncoder
import moped.macros.ClassShape
import moped.macros.ClassShaper

@CommandName("uninstall-completions")
class UninstallCompletionsCommand extends Command {
  override def run(app: Application): Int = {
    ShellCompletion.all(app).foreach { shell =>
      shell.uninstall()
    }
    0
  }
}

object UninstallCompletionsCommand {
  val default = new UninstallCompletionsCommand()

  implicit lazy val parser: CommandParser[UninstallCompletionsCommand] =
    new CodecCommandParser[UninstallCompletionsCommand](
      JsonCodec.encoderDecoderJsonCodec(
        ClassShaper(
          new ClassShape(
            "UninstallCompletionsCommand",
            "moped.commands.UninstallCompletionsCommand",
            List(),
            List(
              Hidden(),
              Description("Uninstall tab completions for bash, zsh and fish")
            )
          )
        ),
        JsonEncoder.stringJsonEncoder.contramap[UninstallCompletionsCommand](
          _ => ""
        ),
        JsonDecoder.constant(default)
      ),
      default
    )
}
