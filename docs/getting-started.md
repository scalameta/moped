---
id: getting-started
title: Getting started
---

Moped is a Scala library to build command-line applications with the following
goals:

- **Model configuration as Scala data structures**: Moped allows you to manage
  all user configuration options as immutable case classes and sealed traits.
- **Limit boilerplate where possible**: Moped provides automatic configuration
  decoders and encoders to help you avoid copy-pasting the same setting name in
  multiple places like the application implementation, configuration parser and
  configuration documentation. Copy-pasting strings is both cumbersome and it
  also increases the risk of making mistakes resulting in a bad end-user
  experience.
- **Evolve configuration without breaking changes**: it's normal that
  configuration options change as your application evolves (naming is hard).
  Moped supports several ways to evolve user configuration options in a
  backwards compatible way so that your existing users have an easier time to
  upgrade to the latest versions of your application.
- **Report helpful error messages**: Moped reports errors using source positions
  in the user-written configuration files, similar to how a compiler reports
  errors.
- **Treat command-line arguments as configuration**: Moped provides a
  command-line parser with automatic generation of `--help` messages, tab
  completions for bash/zsh and more. Command-line arguments map into Scala case
  classes, just like HOCON and JSON configuration.

## Quick start

```scala
libraryDependencies += "org.scalameta" %% "moped" % "@VERSION@"
```

Next, write a case class for your user configuration.

```scala mdoc
import moped.annotations.PositionalArguments
import moped.console.Application
import moped.console.Command
import moped.console.CommandParser

case class EchoCommand(
  uppercase: Boolean = false,
  @PositionalArguments
  arguments: List[String] = Nil
) extends Command {
  def run(app: Application): Int = {
    val toPrint =
      if (uppercase) arguments.map(_.toUpperCase)
      else arguments
    app.info(toPrint.mkString(" "))
    0
  }
}
object HelloConfig {
  implicit lazy val parser = CommandParser.derive(EchoCommand())
}
```
