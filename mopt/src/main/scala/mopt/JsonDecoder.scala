package mopt

import mopt.internal.diagnostics.TypeMismatchDiagnostic
import scala.collection.compat._
import scala.collection.mutable

trait JsonDecoder[A] { self =>

  def decode(context: DecodingContext): JsonDecoder.Result[A]

  final def map[B](fn: A => B): JsonDecoder[B] =
    context => self.decode(context).map(fn)

  final def flatMap[B](fn: A => JsonDecoder.Result[B]): JsonDecoder[B] =
    context => self.decode(context).flatMap(fn)

}
object JsonDecoder {
  type Result[+A] = Either[Diagnostic, A]
  def fromJson[A](expected: String)(
      fn: PartialFunction[JsonElement, Result[A]]
  ): JsonDecoder[A] = { context =>
    fn.applyOrElse[JsonElement, Result[A]](
      context.json,
      _ => Left(new TypeMismatchDiagnostic(expected, context))
    )
  }
  implicit val jsonElementJsonDecoder: JsonDecoder[JsonElement] =
    context => Right(context.json)
  implicit val intJsonDecoder: JsonDecoder[Int] =
    fromJson[Int]("JsonNumber") {
      case JsonNumber(value) => Right(value.toInt)
    }
  implicit val doubleJsonDecoder: JsonDecoder[Double] =
    fromJson[Double]("JsonNumber") {
      case JsonNumber(value) => Right(value)
    }
  implicit val unitJsonDecoder: JsonDecoder[Unit] =
    _ => Right(())

  implicit def arrayJsonDecoder[C[_], A](implicit
      ev: JsonDecoder[A],
      factory: Factory[A, C[A]]
  ): JsonDecoder[C[A]] = { context =>
    context.json match {
      case JsonArray(value) =>
        val successB = factory.newBuilder
        val errorB = List.newBuilder[Diagnostic]
        successB.sizeHint(value.length)
        value.zipWithIndex.foreach {
          case (value, i) =>
            val cursor = SelectIndexCursor(i).withParent(context.cursor)
            ev.decode(DecodingContext(value, cursor)) match {
              case Left(e)  => errorB += e
              case Right(e) => successB += e
            }
        }
        Diagnostic.fromDiagnostics(errorB.result()) match {
          case Some(x) => Left(x)
          case None    => Right(successB.result())
        }
      case _ =>
        Left(new TypeMismatchDiagnostic("JsonArray", context))
    }
  }

  implicit def objectJsonDecoder[A](implicit
      ev: JsonDecoder[A]
  ): JsonDecoder[Map[String, A]] = { context =>
    context.json match {
      case JsonObject(members) =>
        val buf = Map.newBuilder[String, A]
        val errors = mutable.ListBuffer.empty[Diagnostic]
        members.foreach { member =>
          val cursor =
            SelectMemberCursor(member.key.value).withParent(context.cursor)
          ev.decode(DecodingContext(member.value, cursor)) match {
            case Left(error)  => errors += error
            case Right(value) => buf += (member.key.value -> value)
          }
        }
        Diagnostic.fromDiagnostics(errors.result()) match {
          case Some(x) => Left(x)
          case None    => Right(buf.result())
        }
      case _ =>
        Left(new TypeMismatchDiagnostic("JsonObject", context))
    }
  }

}
