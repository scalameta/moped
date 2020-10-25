package tests.json

import moped.json.JsonArray
import moped.json.JsonBoolean
import moped.json.JsonMember
import moped.json.JsonNull
import moped.json.JsonNumber
import moped.json.JsonObject
import moped.json.JsonString
import tests.BaseSuite

abstract class BaseJsonElementSuite extends BaseSuite {
  val nul: JsonNull = JsonNull()
  val string: JsonString = JsonString("string")
  val bool: JsonBoolean = JsonBoolean(true)
  val number: JsonNumber = JsonNumber(42)
  val array: JsonArray = JsonArray(List(nul, number, string))
  val member: JsonMember = JsonMember(string, array)
  val obj: JsonObject = JsonObject(List(member, member))
}
