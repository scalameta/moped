lazy val mopt = project
  .settings(
    libraryDependencies ++= List(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
      "com.lihaoyi" %% "pprint" % "0.5.9",
      "com.lihaoyi" %% "fansi" % "0.2.7",
      "org.typelevel" %% "paiges-core" % "0.3.1"
    )
  )
