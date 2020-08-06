---
id: getting-started
title: Getting started
---

Moped is a Scala library to build command-line applications with the following
goals:

- **Treat command-line arguments as configuration**: Moped abstracts over
  whether you're parsing command-line arguments or parsing JSON/HOCON
  configuration files. This feature allows users to declare default command-line
  flags via system-wide or project-wide configuration files to avoid repeating
  the same flags on every command invocation.
- **Model configuration as Scala data structures**: Moped allows you to manage
  user configuration options as immutable case classes and sealed traits making
  your application easy to extend and reason about.
- **Limit boilerplate where possible**: Moped provides macros that automatically
  generate encoders and decoders to help you avoid copy-pasting the same setting
  name in multiple places like the command-line parser, application
  implementation and website documentation. Copy-pasting strings is both
  cumbersome and it also increases the risk of making mistakes resulting in a
  bad end-user experience.
- **Evolve configuration without breaking changes**: it's normal that
  command-line flags and configuration options change as your application
  evolves (naming is hard, after all). Moped supports several ways to evolve
  configuration in a backwards compatible way making it easier for your users to
  upgrade to the latest version of your application.
- **Embrace tab completions**: tab completions help users stay productive when
  running your command-line application but they're often difficult to install
  and they're difficult to keep up-to-date as your application evolves. Moped
  solves this problem by providing automatic installation  of tab completion
  scripts for bash, zsh and fish on macOS, Linux and Windows.
- **Play nicely with native images**: GraalVM's `native-image` allows you to
  distribute standalone binaries for JVM applications with instant startup on
  macOS, Linux and Windows. Moped includes native-image configuration to ensure
  its classes get correctly linked at build-time or runtime by the native image
  optimizer.
- **Report helpful error messages**: Moped reports errors messages like a
  compiler, with cursors pointing to the user-written input, making it easier
  for users to troubleshoot and fix mistakes.
- **Design for testability**: Moped comes with a testkit library that makes it
  easy to write robust unit tests and integration tests for your command-line
  application that stress stateful interactions with the file system and
  standard output.

## Quick start

```diff
  // build.sbt
  lazy val echo = project
    .settings(
+     libraryDependencies += "org.scalameta" %% "moped" % "@VERSION@"
    )
```

Next, write a case class for your user configuration.

```scala mdoc
import moped.annotations._
import moped.commands._
import moped.console._

@Description("Writes arguments to standard output")
@ExampleUsage(
  """|$ echo Hello world!
     |Hello world!
     |$ echo --uppercase Hello world!
     |HELLO WORLD!
     |""".stripMargin
)
case class EchoCommand(
  @Description("If true, prints the output in UPPERCASE")
  uppercase: Boolean = false,
  @PositionalArguments
  arguments: List[String] = Nil
) extends Command {
  def run(app: Application): Int = {
    val toPrint =
      if (uppercase) arguments.map(_.toUpperCase)
      else arguments
    app.out.println(toPrint.mkString(" "))
    0
  }
}
object HelloCommand {
  implicit lazy val parser = CommandParser.derive(EchoCommand())
  lazy val app = Application(
    binaryName = "echo",
    version = "1.0.0",
    commands = List(
      CommandParser[EchoCommand],
      CommandParser[HelpCommand],
      CommandParser[VersionCommand],
    )
  )
  def main(args: Array[String]): Unit = {
    app.runSingleCommand(args.toList)
  }
}
```

```scala mdoc
HelloCommand.app.runSingleCommand(List("Hello world!"))

HelloCommand.app.runSingleCommand(List("--uppercase", "Hello world!"))

HelloCommand.app.runSingleCommand(List("--help"))

HelloCommand.app.runSingleCommand(List("--version"))
```

Next, add a dependency on Moped testkit to write end-to-end tests for your new
echo application.

```diff
  // build.sbt
  lazy val echo = project
    .settings(
+     libraryDependencies += "org.scalameta" %% "moped-testkit" % "@VERSION@" % Test,
      testFrameworks += new TestFramework("munit.Framework"),
    )
```

Next, add a test suite to verify the application works as expected.

```scala mdoc
class EchoSuite extends moped.testkit.MopedSuite(HelloCommand.app) {
  checkOutput(
    "--uppercase prints output in upper-case",
    List("echo", "--uppercase", "hello world"),
    "HELLO WORLD"
  )
}
```

Finally, let's distribute a native-image binary for our `echo` command using the
sbt-native-image packager.

```diff

```
