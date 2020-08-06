package moped.commands

import moped.console.Command
import moped.console.Application

class NestedCommand extends Command {
  final override def run(app: Application): Int =
    throw new NotImplementedError("nested commands cannot be run directly")
}
