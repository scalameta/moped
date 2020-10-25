package moped.cli

import java.util.concurrent.Future

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.util.Success
import scala.util.control.NonFatal

final class CancelToken private (val onCanceled: Promise[Boolean]) {
  override def toString(): String = s"CancelToken(isCanceled=$isCanceled)"
  final def isCanceled: Boolean =
    onCanceled.isCompleted && onCanceled.future.value == Some(Success(true))
  final def cancel(): Boolean = onCanceled.trySuccess(true)
}

object CancelToken {
  def empty(): CancelToken = new CancelToken(Promise())
  def fromJavaFuture(future: Future[_], mayInterruptIfRunning: Boolean = false)(
      implicit ec: ExecutionContext
  ): CancelToken = {
    create(() => future.cancel(mayInterruptIfRunning))
  }
  def create(
      onCanceled: () => Unit
  )(implicit ec: ExecutionContext): CancelToken = {
    val p = Promise[Boolean]()
    p.future
      .foreach {
        case true =>
          onCanceled()
        case _ =>
      }
    new CancelToken(p)
  }

  def fromIterable(
      iterable: Iterable[CancelToken]
  )(implicit ec: ExecutionContext): CancelToken = {
    create(() => cancelAll(iterable))
  }
  def cancelAll(iterable: Iterable[CancelToken]): Boolean = {
    var errors = mutable.ListBuffer.empty[Throwable]
    var isCanceled = true
    iterable.foreach { cancelable =>
      try {
        isCanceled = cancelable.cancel() && isCanceled
      } catch {
        case ex if NonFatal(ex) =>
          errors += ex
      }
    }
    errors.toList match {
      case head :: tail =>
        tail.foreach { e =>
          if (e ne head) {
            head.addSuppressed(e)
          }
        }
        throw head
      case _ =>
        isCanceled
    }
  }
}
