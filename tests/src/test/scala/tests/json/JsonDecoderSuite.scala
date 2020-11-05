package tests.json

import moped.cli.Command
import moped.cli.CommandParser
import moped.json.JsonCodec

case class NestedList(a: String = "")
object NestedList {
  implicit val codec: JsonCodec[NestedList] = moped
    .macros
    .deriveCodec(NestedList())
}

case class MyClass(
    a: Int = 1,
    nested: List[NestedList] = Nil,
    nestedObject: NestedList = NestedList()
) extends Command {
  def run(): Int = 0
}
object MyClass {
  val default: MyClass = MyClass()
  implicit lazy val parser: CommandParser[MyClass] = CommandParser
    .derive[MyClass](default)
}

class JsonDecoderSuite extends BaseJsonDecoderSuite[MyClass] {

  checkDecoded("a", parseJson("{'a': 2, 'b': 42}"), MyClass(a = 2))

  checkErrorDecoded(
    "fatal-unknown-field",
    parseJson("{'a': 2, 'b': 42}"),
    """|moped.json:1:9 error: unknown field name 'b' with value 42
       |{"a": 2, "b": 42
       |         ^
       |""".stripMargin,
    context = _.withFatalUnknownFields(true)
  )

  checkErrorDecoded(
    "fatal-unknown-field",
    parseJson("{'nested': [{'a': []}]}"),
    """|moped.json:1:18 error: Type mismatch at '.a[0]';
       |  found    : Array
       |  expected : String
       |{"nested": [{"a": []}]
       |                  ^
       |""".stripMargin
  )

  checkErrorDecoded(
    "type-mismatch-object-string",
    parseJson("{'nestedObject': 'value'}"),
    """|moped.json:1:17 error: Type mismatch at '.nestedObject';
       |  found    : String
       |  expected : Object
       |{"nestedObject": "value"
       |                 ^
       |""".stripMargin
  )

}
