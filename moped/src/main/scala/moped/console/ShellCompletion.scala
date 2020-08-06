package moped.console

import java.nio.file.Files
import java.nio.file.Path

import moped.internal.console.Utils

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
      val functionDir = completionFile.getParent()
      Utils.appendLines(
        zshrc,
        List(
          s"if [[ -d '$functionDir' ]]; then fpath=($$fpath '$functionDir'); fi"
        )
      )
    }
  }
  def uninstall(): Unit = {
    if (Files.isRegularFile(zshrc)) {
      Utils.filterLinesMatching(zshrc, completionFile.toString())
    }

  }
  private def zshrc = app.env.homeDirectory.resolve(".zshrc")
  private def completionFile: Path =
    app.cacheDirectory.resolve("zsh").resolve(s"_${app.binaryName}")
  private def completionScript: String =
    """|#compdef _BINARY_NAME BINARY_NAME
       |
       |function _BINARY_NAME {
       |    compadd -- $(BINARY_NAME completions run zsh $CURRENT $words[@] 2> /dev/null)
       |}
       |""".stripMargin.replace("BINARY_NAME", app.binaryName)
}

final class BashCompletion(app: Application) extends ShellCompletion(app) {
  def install(): Unit = {
    Utils.overwriteFile(completionFile, completionScript)
    app.info(completionFile.toString())
    if (Files.isRegularFile(bashrc)) {
      Utils.appendLines(
        bashrc,
        List(s"[ -s '$completionFile' ] && source '$completionFile'")
      )
    }
  }
  def uninstall(): Unit = {
    Utils.filterLinesMatching(bashrc, completionFile.toString())
  }
  private def bashrc = app.env.homeDirectory.resolve(".bashrc")
  private def completionFile: Path =
    app.configDirectory.resolve("bash").resolve(s"${app.binaryName}.sh")
  private def completionScript: String =
    """|_BINARY_NAME() { 
       |  completions=$(BINARY_NAME completions run bash ${#COMP_WORDS[@]} ${COMP_WORDS[@]} 2> /dev/null)
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
       |   BINARY_NAME completions run fish $arguments $current 2> /dev/null
       |end
       |complete -f -c BINARY_NAME -a "(_BINARY_NAME)"
       |""".stripMargin.replace("BINARY_NAME", app.binaryName)
}
