package tests

import moped.internal.console.Cases

class CasesSuite extends BaseSuite {
  def check(camel: String, kebab: String): Unit =
    test(camel) {
      assertEquals(
        kebab,
        Cases.camelToKebab(Cases.kebabToCamel(kebab)),
        s"argument '$kebab' is not kebab case"
      )
      assertEquals(
        camel,
        Cases.kebabToCamel(Cases.camelToKebab(camel)),
        s"argument '$camel' is not camel case"
      )
      assertEquals(Cases.kebabToCamel(kebab), clue(camel))
      assertEquals(Cases.camelToKebab(camel), clue(kebab))
    }

  check("Complete", "complete")
  check("CompleteCommand", "complete-command")
  check("Complete_command", "complete_command")
}
