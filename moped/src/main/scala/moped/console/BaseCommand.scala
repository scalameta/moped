package moped.console

import scala.concurrent.Promise
import scala.concurrent.Future
import scala.util.Try

abstract class BaseCommand {
  def runAsFuture(app: Application): Future[Int]
  def isHidden: Boolean = false
}

abstract class Command extends BaseCommand {
  final override def runAsFuture(app: Application): Future[Int] =
    Future.fromTry(Try(run(app)))
  def run(app: Application): Int
}