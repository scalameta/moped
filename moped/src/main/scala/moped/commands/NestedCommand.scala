package moped.commands

import moped.console.Application
import moped.console.Command

class NestedCommand extends Command {
  def run(app: Application): Int = throw new NotImplementedError()
}
