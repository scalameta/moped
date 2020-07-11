package moped

import scala.concurrent.Promise
import scala.concurrent.Future

abstract class BaseCommand(
    val subcommandName: String
) {
  type F[_]
  def intoFuture(exitCode: F[Int], cancelToken: Promise[Boolean]): Future[Int]
  def run(app: Environment): F[Int]
}

abstract class Command(subcommandName: String)
    extends BaseCommand(subcommandName) {
  type F[A] = A
  override def intoFuture(
      exitCode: Int,
      cancelToken: Promise[Boolean]
  ): Future[Int] = Future.successful(exitCode)
  def run(app: Environment): Int
}
