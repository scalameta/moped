package moped.commands

import moped.console.Command
import moped.console.Application

class NestedCommand extends Command {
  def run(app: Application): Int = throw new NotImplementedError()
}
