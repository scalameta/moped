package moped.console

import scala.concurrent.Promise
import scala.concurrent.Future

abstract class BaseCommand(
    val subcommandName: String
) {
  type F[_]
  def intoFuture(exitCode: F[Int], token: CancelToken): Future[Int]
  def run(app: Application): F[Int]
}

abstract class Command(subcommandName: String)
    extends BaseCommand(subcommandName) {
  final type F[A] = A
  final override def intoFuture(
      exitCode: Int,
      cancelToken: CancelToken
  ): Future[Int] = Future.successful(exitCode)
  def run(app: Application): Int
}
