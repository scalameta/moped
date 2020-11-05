package tests.json

import moped.json.DecodingContext
import moped.json.ErrorResult
import moped.json.JsonCodec
import moped.json.JsonElement
import moped.json.ValueResult
import munit.TestOptions

abstract class BaseJsonDecoderSuite[T]()(implicit ev: JsonCodec[T])
    extends tests.BaseSuite {
  def checkDecoded(
      name: TestOptions,
      original: JsonElement,
      expected: T,
      context: DecodingContext => DecodingContext = identity
  ): Unit = {
    test(name) {
      val obtained = ev.decode(context(DecodingContext(original))).get
      assertEquals(obtained, expected)
    }
  }

  def checkErrorDecoded(
      name: TestOptions,
      original: JsonElement,
      expected: String,
      context: DecodingContext => DecodingContext = identity
  )(implicit loc: munit.Location): Unit = {
    test(name) {
      ev.decode(context(DecodingContext(original))) match {
        case ValueResult(value) =>
          fail(s"expected error, obtained success $value")
        case ErrorResult(error) =>
          val obtained = error.position.pretty("error", error.message)
          assertNoDiff(obtained, expected)
      }
    }
  }

}
