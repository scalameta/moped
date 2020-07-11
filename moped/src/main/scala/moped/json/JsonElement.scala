package moped.json

import scala.collection.mutable
import moped.reporters._
import moped.internal.json.NestedJsonKey

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
      case JsonNull()         => JsonNull()
      case JsonNumber(value)  => JsonNumber(value)
      case JsonBoolean(value) => JsonBoolean(value)
      case JsonString(value)  => JsonString(value)
      case JsonArray(value)   => JsonArray(value)
      case JsonObject(value)  => JsonObject(value)
    }
  // TODO(olafur) rename this method
  final def normalize: JsonElement = {
    def expandKeys(conf: JsonElement): JsonElement =
      conf match {
        case _: JsonPrimitive    => conf
        case JsonArray(elements) => JsonArray(elements.map(_.normalize))
        case JsonObject(members) =>
          val expandedKeys: List[JsonMember] = members.map {
            case JsonMember(NestedJsonKey(key, rest), value) =>
              JsonMember(
                JsonString(key),
                JsonObject(List(JsonMember(JsonString(rest), value.normalize)))
              )
            case JsonMember(key, value) =>
              JsonMember(key, value.normalize)
          }
          JsonObject(expandedKeys)
      }
    expandKeys(this)
  }
}
sealed abstract class JsonPrimitive extends JsonElement
final case class JsonNull() extends JsonPrimitive
final case class JsonNumber(value: Double) extends JsonPrimitive
final case class JsonBoolean(value: Boolean) extends JsonPrimitive
final case class JsonString(value: String) extends JsonPrimitive
final case class JsonArray(elements: List[JsonElement]) extends JsonElement
final case class JsonObject(members: List[JsonMember]) extends JsonElement {
  val value =
    new mutable.LinkedHashMap() ++
      members.iterator.map(m => m.key.value -> m.value)
  def getMember(key: String): Option[JsonElement] = {
    value.get(key)
  }
}
final case class JsonMember(key: JsonString, value: JsonElement)
