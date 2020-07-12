package tests

import munit.FunSuite
import munit.TestOptions
import java.nio.file.Paths
import java.nio.file.Path
import moped.internal.console.PathCompleter
import moped.console.Completer

class CustomCompleter
object CustomCompleter {
  implicit val completer: Completer[CustomCompleter] =
    context => List()
}

case class ExampleClass(
    boolean: Boolean = false,
    option: Option[Boolean] = None,
    list: List[Boolean] = Nil,
    map: Map[String, Boolean] = Map.empty,
    path: Path,
    paths: List[Path],
    workingDirectory: Option[Path],
    custom: CustomCompleter
)

class ClassShapeSuite extends FunSuite {
  val shape = moped.macros.deriveShaper[ExampleClass]
  test("params") {
    assertEquals(
      shape.parametersFlat.map(p => p.name -> p.tpe),
      List[(String, String)](
        "boolean" -> "Boolean",
        "option" -> "Option[Boolean]",
        "list" -> "List[Boolean]",
        "map" -> "Map[String, Boolean]",
        "path" -> "Path",
        "paths" -> "List[Path]",
        "workingDirectory" -> "Option[Path]",
        "custom" -> "CustomCompleter"
      )
    )
  }

  def checkCompleter(key: String, expected: Completer[_]): Unit =
    test(s"completer-$key") {
      assertEquals[Any, Any](
        shape.get(clue(key)).get.tabCompleter.get,
        expected
      )
    }

  checkCompleter("path", PathCompleter)
  checkCompleter("paths", PathCompleter)
  checkCompleter("workingDirectory", PathCompleter)
  checkCompleter("custom", CustomCompleter.completer)

}