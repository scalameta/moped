package moped.console

import moped.annotations.CommandName

@CommandName("install-completions")
class InstallCompletionsCommand extends Command {
  override def run(app: Application): Int = {
    0
  }
}
