val V =
  new {
    val scala213 = "2.13.13"
    val scala212 = "2.12.17"
    val dirs = "26"
    val collectionCompat = "2.11.0"
    val dataClass = "0.2.6"
    val osLib = "0.9.3"
    val ujson = "3.2.0"
    val pprint = "0.8.1"
    val fansi = "0.4.0"
    val paiges = "0.4.3"
    val macroParadise = "2.1.1"
    val sconfig = "1.6.0"
    val snakeyaml = "2.2"
    val tomlScala = "0.2.2"
    val dhallScala = "0.10.0-M2"
    val munit = "0.7.29"
    val scalatags = "0.12.0"
  }

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
    scalaVersion := V.scala213,
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
          "org.scalamacros" % "paradise" % V.macroParadise cross
            CrossVersion.full
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
      "dev.dirs" % "directories" % V.dirs,
      "org.scala-lang.modules" %% "scala-collection-compat" %
        V.collectionCompat,
      "io.github.alexarchambault" %% "data-class" % V.dataClass,
      "com.lihaoyi" %% "os-lib" % V.osLib,
      "com.lihaoyi" %% "ujson" % V.ujson,
      "com.lihaoyi" %% "pprint" % V.pprint,
      "com.lihaoyi" %% "fansi" % V.fansi,
      "org.typelevel" %% "paiges-core" % V.paiges
    ),
  crossScalaVersions := List(V.scala212, V.scala213)
)

lazy val hocon = project
  .in(file("moped-hocon"))
  .settings(
    moduleName := "moped-hocon",
    libraryDependencies ++= List("org.ekrich" %% "sconfig" % V.sconfig),
    crossScalaVersions := List(V.scala212, V.scala213)
  )
  .dependsOn(moped)

lazy val yaml = project
  .in(file("moped-yaml"))
  .settings(
    moduleName := "moped-yaml",
    libraryDependencies ++= List("org.yaml" % "snakeyaml" % V.snakeyaml),
    crossScalaVersions := List(V.scala212, V.scala213)
  )
  .dependsOn(moped)

lazy val toml = project
  .in(file("moped-toml"))
  .settings(
    moduleName := "moped-toml",
    libraryDependencies ++= List("tech.sparse" %% "toml-scala" % V.tomlScala),
    crossScalaVersions := List(V.scala212, V.scala213)
  )
  .dependsOn(moped)

lazy val dhall = project
  .in(file("moped-dhall"))
  .settings(
    moduleName := "moped-dhall",
    libraryDependencies ++= List("org.dhallj" %% "dhall-scala" % V.dhallScala),
    crossScalaVersions := List(V.scala212, V.scala213)
  )
  .dependsOn(moped)

lazy val testkit = project
  .in(file("moped-testkit"))
  .settings(
    moduleName := "moped-testkit",
    libraryDependencies ++= List("org.scalameta" %% "munit" % V.munit),
    crossScalaVersions := List(V.scala212, V.scala213)
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
  .dependsOn(testkit, hocon, toml, yaml, dhall)

lazy val docs = project
  .in(file("moped-docs"))
  .settings(
    moduleName := "moped-docs",
    fork := isCI,
    (publish / skip) := true,
    libraryDependencies += "com.lihaoyi" %% "scalatags" % V.scalatags,
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
