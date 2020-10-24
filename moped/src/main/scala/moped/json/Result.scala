package moped.json

import scala.collection.mutable
import scala.util.control.NonFatal

import moped.internal.diagnostics.WithFilterDiagnostic
import moped.reporters.Diagnostic

final case class ValueResult[+A](value: A) extends Result[A]
final case class ErrorResult(error: Diagnostic) extends Result[Nothing]

object Result {
  def value[A](a: A): Result[A] = ValueResult(a)
  def error[A](d: Diagnostic): Result[A] = ErrorResult(d)
  def fromResults[A](results: Iterable[Result[A]]): Result[List[A]] = {
    val buf = mutable.ListBuffer.empty[A]
    val errors = mutable.ListBuffer.empty[Diagnostic]
    results
      .iterator
      .foreach {
        case ErrorResult(error) =>
          errors += error
        case ValueResult(value) =>
          buf += value
      }
    Diagnostic.fromDiagnostics(errors.toList) match {
      case Some(diagnostic) =>
        ErrorResult(diagnostic)
      case None =>
        ValueResult(buf.toList)
    }
  }
  def fromUnsafe[A](thunk: () => A): Result[A] =
    try ValueResult(thunk())
    catch {
      case NonFatal(e) =>
        ErrorResult(Diagnostic.exception(e))
    }
}

sealed abstract class Result[+A] extends Product with Serializable {

  def isError: Boolean = this.isInstanceOf[ErrorResult]
  def isValue: Boolean = this.isInstanceOf[ValueResult[_]]

  def get: A = fold(identity, d => throw new NoSuchElementException(d.pretty))
  def getOrElse[B >: A](other: => B): B = fold(identity, _ => other)
  def orElse[B >: A](other: => Result[B]): Result[B] =
    fold(ValueResult(_), _ => other)

  def toOption: Option[A] = fold(Some(_), _ => None)
  def toEither: Either[Diagnostic, A] = fold(Right(_), Left(_))
  def toList: List[A] = fold(List(_), _ => List())
  def iterator(): Iterator[A] = fold(Iterator(_), _ => Iterator())

  def foreach[B](fn: A => B): Unit = fold(a => fn(a), _ => ())
  def map[B](fn: A => B): Result[B] =
    fold(a => ValueResult(fn(a)), ErrorResult(_))
  def flatMap[B](fn: A => Result[B]): Result[B] =
    fold(a => fn(a), ErrorResult(_))

  def filter(fn: A => Boolean): Result[A] = withFilter(fn)
  def withFilter(fn: A => Boolean): Result[A] =
    fold(
      a => {
        if (fn(a))
          ValueResult(a)
        else
          ErrorResult(new WithFilterDiagnostic(a, fn))
      },
      ErrorResult(_)
    )

  /**
   * Use this method to upcast this type from `ErrorResult` or `ValueResult`
   * into `Result[A]`
   *
   * @return
   *   this instance unchanged, this method is only used to influence type
   *   checking.
   */
  def upcast: Result[A] = this

  def fold[B](onValue: A => B, onError: Diagnostic => B): B =
    this match {
      case ValueResult(value) =>
        onValue(value)
      case ErrorResult(error) =>
        onError(error)
    }

  def product[B](other: Result[B]): Result[(A, B)] =
    (this, other) match {
      case (ErrorResult(a), ErrorResult(b)) =>
        ErrorResult(a.mergeWith(b))
      case (ErrorResult(a), _) =>
        ErrorResult(a)
      case (_, ErrorResult(b)) =>
        ErrorResult(b)
      case (ValueResult(a), ValueResult(b)) =>
        ValueResult((a, b))
    }

}
