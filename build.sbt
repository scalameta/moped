lazy val fansiVersion = Def.setting {
  if (scalaVersion.value.startsWith("2.11")) "0.2.6"
  else "0.2.7"
}
lazy val mopt = project
  .settings(
    libraryDependencies ++= List(
      "com.lihaoyi" %% "pprint" % "0.5.9",
      "com.lihaoyi" %% "fansi" % fansiVersion.value,
      "org.typelevel" %% "paiges-core" % "0.3.1"
    )
  )
