package moped.json

import scala.collection.mutable

import moped.internal.reporters._
import moped.reporters._
import org.typelevel.paiges.Doc

sealed abstract class JsonElement extends Product with Serializable {
  private var myPosition: Position = NoPosition
  def position = myPosition
  def withPosition(newPosition: Position): JsonElement = {
    val copy = copyThis()
    copy.myPosition = newPosition
    copy
  }
  private def copyThis(): JsonElement =
    this match {
      case JsonNull() => JsonNull()
      case JsonNumber(value) => JsonNumber(value)
      case JsonBoolean(value) => JsonBoolean(value)
      case JsonString(value) => JsonString(value)
      case JsonArray(value) => JsonArray(value)
      case JsonObject(value) => JsonObject(value)
    }
  final def toDoc: Doc =
    this match {
      case JsonNull() => Doc.text("null")
      case JsonNumber(value) => Doc.text(value.toString())
      case JsonBoolean(value) => Doc.text(value.toString())
      case JsonString(value) =>
        Doc.text("\"%s\"".format(JsonFormatters.escape(value)))
      case JsonArray(elements) =>
        if (elements.isEmpty) {
          Doc.text("[]")
        } else {
          val parts = Doc.intercalate(
            Doc.comma,
            elements.map { j =>
              (Doc.line + j.toDoc).grouped
            }
          )
          "[" +: ((parts :+ " ]").nested(2))
        }
      case obj @ JsonObject(members) =>
        val keyValues = obj.value.map {
          case (s, j) =>
            JsonString(s).toDoc + Doc.text(":") + ((Doc.lineOrSpace + j.toDoc)
              .nested(2))
        }
        val parts = Doc.fill(Doc.comma, keyValues)
        parts.bracketBy(Doc.text("{"), Doc.text("}"))
    }

}
sealed abstract class JsonPrimitive extends JsonElement
final case class JsonNull() extends JsonPrimitive
final case class JsonNumber(value: Double) extends JsonPrimitive
final case class JsonBoolean(value: Boolean) extends JsonPrimitive
final case class JsonString(value: String) extends JsonPrimitive
final case class JsonArray(elements: List[JsonElement]) extends JsonElement
final case class JsonObject(members: List[JsonMember]) extends JsonElement {
  val value: mutable.Map[String, JsonElement] =
    new mutable.LinkedHashMap() ++
      members.iterator.map(m => m.key.value -> m.value)
  def getMember(key: String): Option[JsonElement] = {
    value.get(key)
  }
}
final case class JsonMember(key: JsonString, value: JsonElement)
