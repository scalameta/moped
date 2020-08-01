inThisBuild(
  List(
    useSuperShell := false,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.0",
    scalaVersion := "2.12.12",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= List(
      "-Ywarn-unused",
      "-Yrangepos"
    )
  )
)
lazy val moped = project
  .settings(
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
      "com.lihaoyi" %% "pprint" % "0.5.9",
      "com.lihaoyi" %% "fansi" % "0.2.7",
      "org.typelevel" %% "paiges-core" % "0.3.1"
    )
  )

lazy val tests = project
  .settings(
    testFrameworks := List(new TestFramework("munit.Framework")),
    libraryDependencies ++= List(
      "org.scalameta" %% "munit" % "0.7.9"
    )
  )
  .dependsOn(moped)
