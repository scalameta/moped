package moped

import scala.concurrent.Promise

case class Application(
    commands: List[CommandParser[_]]
) {
  val parser = commands.head
  val command = parser.parseCommand(List()).get
  val future =
    command.intoFuture(command.run(Environment()), Promise.successful(false))
}
