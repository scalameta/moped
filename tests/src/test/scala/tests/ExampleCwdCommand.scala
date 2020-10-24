package tests

import moped.annotations.Hidden
import moped.annotations.Inline
import moped.cli.Application
import moped.cli.Command
import moped.cli.CommandParser
import moped.json.JsonCodec

case class SharedCwdOptions(
    @Inline
    app: Application = Application.default
)

object SharedCwdOptions {
  implicit val codec: JsonCodec[SharedCwdOptions] = moped
    .macros
    .deriveCodec(SharedCwdOptions())
}

@Hidden
final case class ExampleCwdCommand(
    @Inline
    shared: SharedCwdOptions = SharedCwdOptions()
) extends Command {
  def app = shared.app
  def run(): Int = {
    app.out.println(s"cwd=${app.env.workingDirectory}")
    0
  }
}

object ExampleCwdCommand {
  implicit val parser: CommandParser[ExampleCwdCommand] = CommandParser
    .derive(ExampleCwdCommand())
}
