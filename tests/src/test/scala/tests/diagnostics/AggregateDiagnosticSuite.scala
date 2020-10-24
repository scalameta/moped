package tests.diagnostics

import moped.internal.diagnostics.AggregateDiagnostic
import moped.reporters.Diagnostic
import munit.TestOptions
import tests.BaseSuite

class AggregateDiagnosticSuite extends BaseSuite {
  def check(
      name: TestOptions,
      errors: List[Diagnostic],
      expectedMessage: String
  ): Unit = {
    test(name) {
      val obtained = new AggregateDiagnostic(errors.head, errors.tail)
      assertNoDiff(obtained.message, expectedMessage)

    }
  }

  check("one", List(Diagnostic.error("one")), "error: one")

  check(
    "two",
    List(Diagnostic.error("one"), Diagnostic.error("two")),
    """|[E1] error: one
       |[E2] error: two
       |2 errors
       |""".stripMargin
  )

  check(
    "three",
    List(
      Diagnostic.error("one"),
      Diagnostic.error("two"),
      Diagnostic.error("three")
    ),
    """|[E1] error: one
       |[E2] error: two
       |[E3] error: three
       |3 errors
       |""".stripMargin
  )
}
