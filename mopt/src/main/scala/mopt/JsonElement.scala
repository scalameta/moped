package mopt

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
}
sealed abstract class JsonPrimitive extends JsonElement
final case class JsonNull() extends JsonPrimitive
final case class JsonNumber(value: Double) extends JsonPrimitive
final case class JsonBoolean(value: Boolean) extends JsonPrimitive
final case class JsonString(value: String) extends JsonPrimitive
final case class JsonArray(elements: List[JsonElement]) extends JsonElement
final case class JsonObject(members: List[JsonMember]) extends JsonElement
final case class JsonMember(key: JsonString, value: JsonElement)
