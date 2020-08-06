addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.2")
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.3")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.19")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.0")

unmanagedSourceDirectories.in(Compile) +=
  baseDirectory.in(ThisBuild).value.getParentFile /
    "moped-sbt" / "src" / "main" / "scala"
unmanagedResourceDirectories.in(Compile) +=
  baseDirectory.in(ThisBuild).value.getParentFile /
    "moped-sbt" / "src" / "main" / "resources"
