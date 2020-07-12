package tests

import munit.FunSuite
import munit.TestOptions
import java.nio.file.Paths
import java.nio.file.Path
import moped.internal.console.PathCompleter

case class ExampleClass(
    boolean: Boolean = false,
    option: Option[Boolean] = None,
    list: List[Boolean] = Nil,
    map: Map[String, Boolean] = Map.empty,
    path: Path,
    paths: List[Path],
    workingDirectory: Option[Path]
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
        "workingDirectory" -> "Option[Path]"
      )
    )
  }

  List("path", "paths", "workingDirectory").foreach { key =>
    test(s"completer-$key") {
      assertEquals[Any, Any](
        shape.get(clue(key)).get.tabCompleter.get,
        PathCompleter
      )
    }
  }

}
