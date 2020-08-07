package moped.console

import java.io.BufferedReader
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.{util => ju}

import scala.collection.JavaConverters._

final case class Environment(
    standardOutput: PrintStream = Console.out,
    standardError: PrintStream = Console.err,
    standardInput: BufferedReader = Console.in,
    workingDirectory: Path = Paths.get(System.getProperty("user.dir")),
    homeDirectory: Path = Paths.get(System.getProperty("user.home")),
    configDirectory: Path = Paths.get(System.getProperty("user.home")),
    systemProperties: ju.Properties = System.getProperties(),
    environmentVariables: collection.Map[String, String] =
      System.getenv().asScala
)

object Environment {
  val default: Environment = Environment()
}
