package sbtmoped

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.io.File
import scala.collection.mutable
import scala.sys.process.Process

object MopedPlugin extends AutoPlugin {
  override def requires = JvmPlugin
  object autoImport {
    val Moped: Configuration = config("moped")
    lazy val mopedGraalJVM: TaskKey[String] =
      taskKey[String](
        "The GraalVM to use, defaults to graalvm:$mopedGraalVersion"
      )
    lazy val mopedGraalVersion: TaskKey[String] =
      taskKey[String]("The version of GraalVM to use by default")
    lazy val mopedNativeImageBinary: TaskKey[List[String]] =
      taskKey[List[String]]("The path to the GraalVM native-image binary")
    lazy val mopedNativeImage: TaskKey[File] =
      taskKey[File]("Generate a native image for this project.")
    lazy val mopedNativeImageOptions: TaskKey[List[String]] =
      taskKey[List[String]](
        "Extra command-line arguments that should be forwarded to the native-image optimizer"
      )
  }
  import autoImport._
  override def projectSettings: Seq[Def.Setting[_]] =
    List(
      target.in(Moped) := target.in(Compile).value / "moped",
      mainClass.in(Moped) := mainClass.in(Compile).value,
      mopedGraalVersion := "20.1.0",
      mopedGraalJVM := s"graalvm:${mopedGraalVersion.value}",
      name.in(Moped) := name.value,
      mainClass.in(Moped) := mainClass.in(Compile).value,
      mopedNativeImageOptions := List(),
      mopedNativeImageBinary := {
        val out = target.in(Moped).value / "moped-internal" / "coursier"
        Files.createDirectories(out.toPath.getParent)
        val in =
          this.getClass().getResourceAsStream("/sbt-moped/coursier")
        Files.copy(
          in,
          out.toPath,
          StandardCopyOption.REPLACE_EXISTING
        )
        out.setExecutable(true)
        val svmVersion = mopedGraalVersion.value
        List(
          out.toString(),
          "launch",
          "--jvm",
          mopedGraalJVM.value,
          s"org.graalvm.nativeimage:svm-driver:$svmVersion",
          "--"
        )
      },
      mopedNativeImage := {
        val cp = fullClasspath
          .in(Compile)
          .value
          .map(_.data)
          .mkString(File.pathSeparator)
        val command = mutable.ListBuffer.empty[String]
        val main = mainClass
          .in(Moped)
          .value
          .getOrElse(
            throw new MessageOnlyException(
              "no mainClass is specified. " +
                "To fix this problem, update build.sbt to include the settings " +
                "`mainClass.in(Compile) := Some(\"com.MainClass\")`"
            )
          )
        val binaryName = target.in(Moped).value / name.in(Moped).value
        command ++= mopedNativeImageBinary.value
        command += "-cp"
        command += cp
        command ++= mopedNativeImageOptions.value
        command += main
        command += binaryName.absolutePath
        streams.value.log.info(command.mkString(" "))
        val exit = Process(command, cwd = Some(target.in(Moped).value)).!
        if (exit != 0) {
          throw new MessageOnlyException(
            s"native-image command failed with exit code '$exit'"
          )
        }
        binaryName
      }
    )
}
