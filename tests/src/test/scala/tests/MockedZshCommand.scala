package tests

import java.nio.file.Files

import moped.cli.Application
import moped.cli.Command
import moped.cli.ZshCompletion

class MockedZshCommand(app: Application) extends Command {
  def run(): Int = {
    if (app.relativeArguments == ZshCompletion.printFunctionPath.tail) {
      val functions = Files
        .createDirectories(app.env.dataDirectory.resolve("zsh-functions"))
      app.out.println(functions.toString())
      0
    } else {
      app.error(s"unsupported command: $app")
      1
    }
  }
}
