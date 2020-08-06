package tests

class ApplicationSuite extends BaseSuite {
  test("foo".only) {
    val exit = app().run(List("echo", "--uppercase", "Hello world!"))
    assertEquals(exit, 0)
    assertNoDiff(app.capturedOutput, "HELLO WORLD!")
  }
}
