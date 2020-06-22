package mopt

import mopt.internal.diagnostics.TypeMismatchDiagnostic
import scala.collection.compat._
import scala.collection.mutable
import java.nio.file.Path

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
  implicit val stringJsonDecoder: JsonDecoder[String] =
    fromJson[String]("JsonString") {
      case JsonString(value) => Right(value)
    }
  implicit val booleanJsonDecoder: JsonDecoder[Boolean] =
    fromJson[Boolean]("JsonBoolean") {
      case JsonBoolean(value) => Right(value)
    }
  implicit val unitJsonDecoder: JsonDecoder[Unit] =
    _ => Right(())
  implicit val pathJsonDecoder: JsonDecoder[Path] =
    _ => Right(???)

  implicit def arrayJsonDecoder[C[_], A](implicit
      ev: JsonDecoder[A],
      factory: Factory[A, C[A]]
  ): JsonDecoder[C[A]] = { context =>
    context.json match {
      case JsonArray(value) =>
        val successValues = factory.newBuilder
        val errors = List.newBuilder[Diagnostic]
        successValues.sizeHint(value.length)
        value.zipWithIndex.foreach {
          case (value, i) =>
            val cursor = SelectIndexCursor(i).withParent(context.cursor)
            ev.decode(DecodingContext(value, cursor)) match {
              case Left(e)  => errors += e
              case Right(e) => successValues += e
            }
        }
        Diagnostic.fromDiagnostics(errors.result()) match {
          case Some(x) => Left(x)
          case None    => Right(successValues.result())
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
        val successValues = Map.newBuilder[String, A]
        val errors = mutable.ListBuffer.empty[Diagnostic]
        members.foreach { member =>
          val cursor =
            SelectMemberCursor(member.key.value).withParent(context.cursor)
          ev.decode(DecodingContext(member.value, cursor)) match {
            case Left(error)  => errors += error
            case Right(value) => successValues += (member.key.value -> value)
          }
        }
        Diagnostic.fromDiagnostics(errors.result()) match {
          case Some(x) => Left(x)
          case None    => Right(successValues.result())
        }
      case _ =>
        Left(new TypeMismatchDiagnostic("JsonObject", context))
    }
  }

  implicit def optionJsonDecoder[A](implicit
      ev: JsonDecoder[A]
  ): JsonDecoder[Option[A]] = { context =>
    context.json match {
      case JsonNull() => Right(None)
      case other      => ev.decode(context).map(Some(_))
    }
  }

}
