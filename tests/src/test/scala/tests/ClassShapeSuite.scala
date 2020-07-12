package tests

import munit.FunSuite
import munit.TestOptions
import java.nio.file.Paths
import java.nio.file.Path

case class ExampleClass(
    boolean: Boolean = false,
    option: Option[Boolean] = None,
    list: List[Boolean] = Nil,
    map: Map[String, Boolean] = Map.empty,
    path: Path
)

class ClassShapeSuite extends FunSuite {
  val shape = moped.macros.deriveShaper[ExampleClass]
  def check(name: TestOptions, condition: => Unit)(implicit
      loc: munit.Location
  ): Unit =
    test(name) {
      condition
    }
  check(
    "params",
    assertEquals(
      shape.parametersFlat.map(_.name),
      List(
        "boolean",
        "option",
        "list",
        "map",
        "path"
      )
    )
  )
  pprint.log(shape)
}
