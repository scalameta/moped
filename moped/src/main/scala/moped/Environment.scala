package moped

import java.io.InputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.{util => ju}
import scala.collection.JavaConverters._

final case class Environment(
    standardOutput: PrintStream = System.out,
    standardError: PrintStream = System.err,
    standardInput: InputStream = System.in,
    workingDirectory: Path = Paths.get(System.getProperty("user.dir")),
    systemProperties: ju.Properties = System.getProperties(),
    environmentVariables: collection.Map[String, String] =
      System.getenv().asScala
)

object Environment {
  val default = Environment()
}
