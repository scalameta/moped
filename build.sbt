inThisBuild(
  List(
    useSuperShell := false,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.0",
    scalaVersion := "2.12.12",
    scalafixCaching := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= List(
      "-Ywarn-unused:imports",
      "-Yrangepos"
    )
  )
)

crossScalaVersions := Nil
skip.in(publish) := true

lazy val moped = project
  .settings(
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
      "dev.dirs" % "directories" % "20",
      "com.lihaoyi" %% "pprint" % "0.5.9",
      "com.lihaoyi" %% "fansi" % "0.2.7",
      "org.typelevel" %% "paiges-core" % "0.3.1"
    )
  )

lazy val testkit = project
  .settings(
    libraryDependencies ++= List(
      "org.scalameta" %% "munit" % "0.7.10"
    )
  )
  .dependsOn(moped)

lazy val tests = project
  .settings(
    skip.in(publish) := true,
    testFrameworks := List(new TestFramework("munit.Framework")),
    buildInfoPackage := "tests",
    buildInfoKeys := Seq[BuildInfoKey](
      "expectDirectory" -> sourceDirectory.in(Test).value./("expect")
    ),
    mainClass.in(GraalVMNativeImage) := Some(
      "tests.EchoCommand"
    ),
    graalVMNativeImageCommand ~= { old =>
      import scala.util.Try
      import java.nio.file.Paths
      import scala.sys.process._
      Try {
        val jabba = Paths
          .get(sys.props("user.home"))
          .resolve(".jabba")
          .resolve("bin")
          .resolve("jabba")
        val home = s"$jabba which --home graalvm@20.0.0".!!.trim()
        Paths.get(home).resolve("bin").resolve("native-image").toString
      }.getOrElse(old)
    },
    graalVMNativeImageOptions ++= {
      val reflectionFile =
        Keys.sourceDirectory.in(Compile).value./("graal")./("reflection.json")
      assert(reflectionFile.exists, "no such file: " + reflectionFile)
      List(
        "-H:+ReportUnsupportedElementsAtRuntime",
        "--initialize-at-build-time",
        "--no-server",
        "--enable-http",
        "--enable-https",
        "-H:EnableURLProtocols=http,https",
        "--enable-all-security-services",
        "--no-fallback",
        s"-H:ReflectionConfigurationFiles=$reflectionFile",
        "--allow-incomplete-classpath",
        "-H:+ReportExceptionStackTraces"
      )
    }
  )
  .enablePlugins(BuildInfoPlugin, GraalVMNativeImagePlugin)
  .dependsOn(testkit)

addCommandAlias(
  "native-image",
  "; tests/graalvm-native-image:packageBin ; taskready"
)

commands += Command.command("taskready") { s =>
  import scala.sys.process._
  if (System.getenv("CI") == null) {
    scala.util.Try("say 'native-image ready'".!)
  }
  s
}
