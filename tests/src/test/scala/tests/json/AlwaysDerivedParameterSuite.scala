package tests.json

import moped.json.AlwaysDerivedParameter
import moped.json.JsonCodec

case class ExampleAlwaysDerived(n: Int = 42, m: Int = 2)
    extends AlwaysDerivedParameter {
  def isDefault: Boolean = this == ExampleAlwaysDerived.default
  def customCodec: ExampleAlwaysDerived =
    if (isDefault)
      ExampleAlwaysDerived(1337)
    else
      this
}
object ExampleAlwaysDerived {
  val default: ExampleAlwaysDerived = ExampleAlwaysDerived()
  val custom = default.customCodec
  implicit val codec: JsonCodec[ExampleAlwaysDerived] = moped
    .macros
    .deriveCodec(default)
    .bimap[ExampleAlwaysDerived](identity, _.customCodec)
}

case class ExampleAlwaysDerivedNested(
    always: ExampleAlwaysDerived = ExampleAlwaysDerived.default
) {
  def customCodec: ExampleAlwaysDerivedNested =
    copy(always = always.customCodec)
}
object ExampleAlwaysDerivedNested {
  implicit val codec: JsonCodec[ExampleAlwaysDerivedNested] = moped
    .macros
    .deriveCodec(ExampleAlwaysDerivedNested())
}

case class ExampleAlwaysDerivedCommand(
    always: ExampleAlwaysDerived = ExampleAlwaysDerived.default,
    nested: ExampleAlwaysDerivedNested = ExampleAlwaysDerivedNested()
)

object ExampleAlwaysDerivedCommand {
  implicit val codec: JsonCodec[ExampleAlwaysDerivedCommand] = moped
    .macros
    .deriveCodec(ExampleAlwaysDerivedCommand())
}
class AlwaysDerivedParameterSuite
    extends BaseJsonDecoderSuite[ExampleAlwaysDerivedCommand] {
  val default: ExampleAlwaysDerivedCommand = ExampleAlwaysDerivedCommand()
  val custom: ExampleAlwaysDerivedCommand = default.copy(
    always = default.always.customCodec,
    nested = default.nested.customCodec
  )

  checkErrorDecoded(
    "direct-always-derive",
    parseJson("{'always': {'a': 33}}"),
    """|moped.json:1:12 error: unknown field name 'a' with value 33
       |{"always": {"a": 33}
       |            ^
       |""".stripMargin,
    context = _.withFatalUnknownFields(true)
  )

  test("confidence-check") {
    assertNotEquals(custom, default)
  }

  // assert that the custom decoder is always triggered even if input is empty.
  checkDecoded("default", parseJson("{}"), custom)

  checkDecoded(
    "direct-always-derive",
    parseJson("{'always': {'n': 33}}"),
    custom.copy(always = ExampleAlwaysDerived(n = 33))
  )

  checkDecoded(
    "nested-always-derive",
    parseJson("{'nested': {'always': {'n': 33}}}"),
    custom
      .copy(nested = custom.nested.copy(always = ExampleAlwaysDerived(n = 33)))
  )
}
