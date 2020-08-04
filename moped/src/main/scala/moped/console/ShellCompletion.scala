package moped.console

import java.nio.file.Path
import moped.internal.console.Utils
import java.nio.file.Files

sealed abstract class ShellCompletion(app: Application) {
  def install(): Unit
  def uninstall(): Unit
}
object ShellCompletion {
  def all(app: Application): List[ShellCompletion] =
    List(
      new BashCompletion(app),
      new ZshCompletion(app),
      new FishCompletion(app)
    )
}

final class ZshCompletion(app: Application) extends ShellCompletion(app) {
  def install(): Unit = {
    Utils.overwriteFile(completionFile, completionScript)
    app.info(completionFile.toString())
    if (Files.isRegularFile(zshrc)) {
      Utils.appendFile(
        zshrc,
        s"[ -s '$completionFile' ] && source '$completionFile'"
      )
    }
  }
  def uninstall(): Unit = {
    if (Files.isRegularFile(zshrc)) {
      val query = completionFile.toString()
      val before = Utils.readFile(zshrc)
      val after =
        before.linesIterator.filterNot(_.contains(query)).mkString("\n")
      if (before != after) {
        Utils.overwriteFile(zshrc, after)
      }
    }

  }
  private def zshrc = app.env.homeDirectory.resolve(".zshrc")
  private def completionFile: Path =
    app.configDirectory.resolve("zsh").resolve(s"_${app.binaryName}")
  private def completionScript: String =
    """|#compdef _BINARY_NAME BINARY_NAME
       |
       |function _BINARY_NAME {
       |    compadd -- $(BINARY_NAME complete zsh $CURRENT $words[@] 2> /dev/null)
       |}
       |""".stripMargin.replace("BINARY_NAME", app.binaryName)
}

final class BashCompletion(app: Application) extends ShellCompletion(app) {
  def install(): Unit = {
    Utils.overwriteFile(completionFile, completionScript)
    app.info(completionFile.toString())
    if (Files.isRegularFile(bashrc)) {
      Utils.appendFile(
        bashrc,
        s"[ -s '$completionFile' ] && source '$completionFile'"
      )
    }
  }
  def uninstall(): Unit = {}
  private def bashrc = app.env.homeDirectory.resolve(".bashrc")
  private def completionFile: Path =
    app.configDirectory.resolve("zsh").resolve(s"_${app.binaryName}")
  private def completionScript: String =
    """|_BINARY_NAME() { 
       |  completions=$(BINARY_NAME complete bash ${#COMP_WORDS[@]} ${COMP_WORDS[@]} 2> /dev/null)
       |  cur="${COMP_WORDS[COMP_CWORD]}"
       |  COMPREPLY=($(compgen -W "$completions" -- $cur))
       |  return 0
       |}
       |complete -F _tests tests
       |""".stripMargin.replace("BINARY_NAME", app.binaryName)
}

final class FishCompletion(app: Application) extends ShellCompletion(app) {
  def install(): Unit = {
    if (Files.isDirectory(completionFile.getParent())) {
      Utils.overwriteFile(completionFile, completionScript)
      app.info(completionFile.toString())
    }
  }
  def uninstall(): Unit = {
    Files.deleteIfExists(completionFile)
  }
  private def completionFile: Path =
    app.env.homeDirectory
      .resolve(".config")
      .resolve("fish")
      .resolve("functions")
      .resolve(s"${app.binaryName}.fish")
  private def completionScript: String =
    """|function _BINARY_NAME
       |   set -l arguments (commandline -poc)
       |   set -l current (commandline -ct)
       |   BINARY_NAME complete fish $arguments $current 2> /dev/null
       |end
       |complete -f -c BINARY_NAME -a "(_BINARY_NAME)"
       |""".stripMargin.replace("BINARY_NAME", app.binaryName)
}
