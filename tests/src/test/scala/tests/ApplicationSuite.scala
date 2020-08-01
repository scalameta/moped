package tests

class ApplicationSuite extends BaseSuite {
  test("foo") {
    val exit = app().run(List("echo", "--verbose", "Hello world!"))
    assertEquals(exit, 0)
    assertNoDiff(app.capturedOutput, "HELLO WORLD!")
  }
}
