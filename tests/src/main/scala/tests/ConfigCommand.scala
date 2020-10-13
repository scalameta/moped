package tests

import moped.annotations.Inline
import moped.cli.Application
import moped.cli.Command
import moped.cli.CommandParser
import moped.json.JsonCodec

case class InlineConfig(
    useSuperShell: Boolean = true
)

object InlineConfig {
  implicit val codec: JsonCodec[InlineConfig] = moped.macros.deriveCodec(InlineConfig())
}

case class ConfigCommand(
    foobar: Boolean = false,
    @Inline in: InlineConfig = InlineConfig(),
    app: Application = Application.default
) extends Command {
  def run(): Int = {
    if (foobar) app.out.println("foobar")
    else app.out.println("no foobar")
    pprint.log(in.useSuperShell)
    if (!in.useSuperShell) app.out.println("--no-use-super-shell")
    0
  }
}

object ConfigCommand {
  implicit lazy val parser: CommandParser[ConfigCommand] =
    CommandParser.derive(ConfigCommand())
}
