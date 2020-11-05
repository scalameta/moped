package moped.internal.json

import moped.internal.diagnostics.MissingFieldDiagnostic
import moped.internal.diagnostics.TypeMismatchDiagnostic
import moped.json._
import moped.macros.ParameterShape

object DrillIntoJson {
  def decodeAlwaysDerivedParameter[T](key: String, context: DecodingContext)(
      implicit ev: JsonDecoder[T]
  ): Result[T] = {
    ev.decode(
      context
        .withJson(getKey(context.json, List(key)).getOrElse(JsonObject(Nil)))
        .withCursor(SelectMemberCursor(key).withParent(context.cursor))
    )
  }
  def getKey(obj: JsonElement, keys: Seq[String]): Option[JsonElement] =
    if (keys.isEmpty)
      None
    else {
      obj match {
        case obj @ JsonObject(_) =>
          obj
            .members
            .collectFirst {
              case JsonMember(JsonString(key), value) if key == keys.head =>
                value
            }
            .orElse(getKey(obj, keys.tail))
        case _ =>
          None
      }
    }

  def decodeMember[T](context: DecodingContext, member: String, default: T)(
      implicit ev: JsonDecoder[T]
  ): Result[T] = {
    getKey(context.json, List(member)) match {
      case Some(value) =>
        ev.decode(context.withJson(value).withSelectMemberCursor(member))
      case None =>
        ValueResult(default)
    }
  }
  def getOrElse[T](
      conf: JsonElement,
      default: T,
      param: ParameterShape,
      context: DecodingContext
  )(implicit ev: JsonDecoder[T]): Result[T] = {
    getKey(conf, param.allNames) match {
      case Some(value) =>
        ev.decode(context.withJson(value))
      case None =>
        if (param.isAlwaysDerived)
          ev.decode(context.withJson(JsonObject(Nil)))
        else
          ValueResult(default)
    }
  }

  def get[T](context: DecodingContext, path: String, extraNames: String*)(
      implicit ev: JsonDecoder[T]
  ): Result[T] = {
    getKey(context.json, path +: extraNames) match {
      case Some(value) =>
        ev.decode(context.withJson(value))
      case None =>
        context.json match {
          case JsonObject(_) =>
            ErrorResult(new MissingFieldDiagnostic(context))
          case _ =>
            ErrorResult(
              new TypeMismatchDiagnostic(
                s"JsonObject with field $path",
                context
              )
            )
        }
    }
  }

}
