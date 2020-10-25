package tests.json

import java.nio.file.Paths

import moped.json.JsonEncoder
import moped.json.JsonNull
import moped.json.JsonNumber
import moped.json.JsonString

class JsonEncoderSuite extends BaseJsonElementSuite {
  test("json-element") {
    assertEquals(JsonEncoder.encode(nul), nul)
    assertEquals(JsonEncoder.encode(string), string)
    assertEquals(JsonEncoder.encode(bool), bool)
    assertEquals(JsonEncoder.encode(number), number)
    assertEquals(JsonEncoder.encode(array), array)
    assertEquals(JsonEncoder.encode(obj), obj)
  }
  test("double") {
    assertEquals(JsonEncoder.encode(2.2), JsonNumber(2.2))
  }
  test("float") {
    assertEquals(JsonEncoder.encode(2.2f), JsonNumber(2.2f))
  }

  test("path") {
    assertEquals(
      JsonEncoder.encode(Paths.get("my-path")),
      JsonString("my-path")
    )
  }

  test("map") {
    assertEquals(
      JsonEncoder.encode(Map("hello" -> Map("world" -> "!"))),
      parseJson("""{"hello": {"world": "!"}}""")
    )
  }

  test("option") {
    assertEquals(JsonEncoder.encode(Some(42)), JsonNumber(42))
    assertEquals(JsonEncoder.encode(Option(42)), JsonNumber(42))
    assertEquals(JsonEncoder.encode(None), JsonNull())
    assertEquals(JsonEncoder.encode(None: Option[Int]), JsonNull())
  }
}
