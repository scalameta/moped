package moped

import scala.concurrent.Promise
import scala.runtime.EmptyMethodCache

case class Application(
    binaryName: String,
    version: String,
    commands: List[CommandParser[_]],
    env: Environment = Environment.default,
    token: CancelToken = CancelToken.empty()
) {
  val parser = commands.head
  val command = parser.parseCommand(List()).get
  val future = command.intoFuture(command.run(this), CancelToken.empty())
}
