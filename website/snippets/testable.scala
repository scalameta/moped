class EchoSuite extends moped.testkit.MopedSuite {
  test("--uppercase prints output in upper-case") {
    val exitCode = app().run(List("echo", "--uppercase", "hello world"))
    assertEquals(exitCode, 0, clues(app.capturedOutput))
    assertNoDiff(app.capturedOutput, "HELLO WORLD")
  }
}
