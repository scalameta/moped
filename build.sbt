def scala212 = "2.12.12"
def scala213 = "2.13.3"
def isCI = "true".equalsIgnoreCase(System.getenv("CI"))
inThisBuild(
  List(
    organization := "org.scalameta",
    homepage := Some(url("https://github.com/scalameta/moped")),
    licenses :=
      List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers :=
      List(
        Developer(
          "olafurpg",
          "Ólafur Páll Geirsson",
          "olafurpg@gmail.com",
          url("https://geirsson.com")
        )
      ),
    useSuperShell := false,
    scalaVersion := scala213,
    scalafixDependencies +=
      "com.github.liancheng" %% "organize-imports" % "0.4.4",
    scalafixCaching := true,
    scalafixScalaBinaryVersion := scalaBinaryVersion.value,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    semanticdbOptions += s"-P:semanticdb:sourceroot:${baseDirectory.value}",
    scalacOptions ++= List("-Ywarn-unused:imports", "-Yrangepos")
  )
)

commands +=
  Command.command("fixAll") { s =>
    "scalafixAll" :: "scalafmtAll" :: "scalafmtSbt" :: s
  }

commands +=
  Command.command("checkAll") { s =>
    "scalafmtCheckAll" :: "scalafmtSbtCheck" :: "scalafixAll --check" ::
      "publishLocal" :: "docs/docusaurusCreateSite" :: s
  }

crossScalaVersions := Nil
(publish / skip) := true
lazy val isAtLeastScala213 = Def.setting {
  import Ordering.Implicits._
  CrossVersion.partialVersion(scalaVersion.value).exists(_ >= (2, 13))
}

lazy val moped = project.settings(
  libraryDependencies ++= {
    if (isAtLeastScala213.value)
      Nil
    else
      Seq(
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
        )
      )
  },
  scalacOptions ++= {
    if (isAtLeastScala213.value)
      Seq("-Ymacro-annotations")
    else
      Nil
  },
  libraryDependencies ++=
    List(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "dev.dirs" % "directories" % "21",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
      "io.github.alexarchambault" %% "data-class" % "0.2.5",
      "com.lihaoyi" %% "os-lib" % "0.7.1",
      "com.lihaoyi" %% "ujson" % "1.2.2",
      "com.lihaoyi" %% "pprint" % "0.6.0",
      "com.lihaoyi" %% "fansi" % "0.2.9",
      "org.typelevel" %% "paiges-core" % "0.3.2"
    ),
  crossScalaVersions := List(scala212, scala213)
)

lazy val hocon = project
  .in(file("moped-hocon"))
  .settings(
    moduleName := "moped-hocon",
    libraryDependencies ++= List("org.ekrich" %% "sconfig" % "1.3.4"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val yaml = project
  .in(file("moped-yaml"))
  .settings(
    moduleName := "moped-yaml",
    libraryDependencies ++= List("org.yaml" % "snakeyaml" % "1.26"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val toml = project
  .in(file("moped-toml"))
  .settings(
    moduleName := "moped-toml",
    libraryDependencies ++= List("tech.sparse" %% "toml-scala" % "0.2.2"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val dhall = project
  .in(file("moped-dhall"))
  .settings(
    moduleName := "moped-dhall",
    libraryDependencies ++= List("org.dhallj" %% "dhall-scala" % "0.7.0-M1"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val jsonnet = project
  .in(file("moped-jsonnet"))
  .settings(
    moduleName := "moped-jsonnet",
    libraryDependencies ++= List("com.lihaoyi" %% "sjsonnet" % "0.2.6"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val testkit = project
  .in(file("moped-testkit"))
  .settings(
    moduleName := "moped-testkit",
    libraryDependencies ++= List("org.scalameta" %% "munit" % "0.7.18"),
    crossScalaVersions := List(scala212, scala213)
  )
  .dependsOn(moped)

lazy val tests = project
  .settings(
    (publish / skip) := true,
    testFrameworks := List(new TestFramework("munit.Framework")),
    buildInfoPackage := "tests",
    buildInfoKeys :=
      Seq[BuildInfoKey](
        "expectDirectory" -> (Test / sourceDirectory).value./("expect")
      ),
    (Compile / mainClass) := Some("tests.EchoCommand"),
    nativeImageOptions ++=
      List(
        "--initialize-at-build-time",
        "--report-unsupported-elements-at-runtime"
      )
  )
  .enablePlugins(BuildInfoPlugin, NativeImagePlugin)
  .dependsOn(testkit, hocon, toml, yaml, dhall, jsonnet)

val scalatagsVersion = Def.setting {
  if (scalaVersion.value.startsWith("2.11"))
    "0.6.7"
  else
    "0.7.0"
}

lazy val plugin = project
  .in(file("moped-sbt"))
  .settings(
    sbtPlugin := true,
    moduleName := "sbt-moped",
    buildInfoPackage := "sbtmoped",
    buildInfoKeys := Seq[BuildInfoKey](version)
  )
  .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("moped-docs"))
  .settings(
    moduleName := "moped-docs",
    fork := isCI,
    (publish / skip) := true,
    libraryDependencies ++=
      List("com.lihaoyi" %% "scalatags" % scalatagsVersion.value),
    mdocVariables :=
      Map(
        "VERSION" -> version.value.replaceFirst("\\+.*", ""),
        "NATIVE_IMAGE_PLUGIN" -> "???",
        "SCALA_VERSION" -> scalaVersion.value
      ),
    mdocOut :=
      (ThisBuild / baseDirectory).value / "website" / "target" / "docs",
    mdocExtraArguments := {
      val cwd = (ThisBuild / baseDirectory).value
      List(
        "--no-link-hygiene",
        "--in",
        (cwd / "docs").getAbsolutePath,
        "--out",
        (cwd / "website" / "target" / "docs").getAbsolutePath,
        "--in",
        (cwd / "blog").getAbsolutePath,
        "--out",
        (cwd / "website" / "blog").getAbsolutePath
      )
    }
  )
  .dependsOn(tests)
  .enablePlugins(DocusaurusPlugin)

addCommandAlias("native-image", "tests/mopedNativeImage")
addCommandAlias("scalafixCheckAll", "scalafixAll --check")
