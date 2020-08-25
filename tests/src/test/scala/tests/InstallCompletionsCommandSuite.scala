package tests

import moped.testkit.FileLayout

class InstallCompletionsCommandSuite extends BaseSuite {
  test("install and uninstall") {
    FileLayout.fromString(
      """|/.zshrc
         |# Comment
         |/.bashrc
         |# Comment
         |/.config/fish/functions/example.fish
         |# Comment
         |""".stripMargin,
      homeDirectory
    )
    assertEquals(app().run(List("completions", "install")), 0)
    assertNoDiff(
      app.capturedOutput,
      """|info: /data/bash/tests.sh
         |info: /data/zsh-functions/_tests
         |info: /home/.config/fish/functions/tests.fish
         |""".stripMargin
    )
    assertNoDiff(
      FileLayout.asString(homeDirectory),
      """|/.config/fish/functions/example.fish
         |# Comment
         |
         |/.config/fish/functions/tests.fish
         |# DO NOT EDIT: this script is automatically generated by the command 'tests completions install'.
         |function _tests
         |   set -l arguments (commandline -poc)
         |   set -l current (commandline -ct)
         |   tests completions run fish-v1 $arguments $current 2> /dev/null
         |end
         |complete -f -c tests -a "(_tests)"
         |
         |/.bashrc
         |# Comment
         |[ -s '/data/bash/tests.sh' ] && source '/data/bash/tests.sh'
         |
         |/.zshrc
         |# Comment
         |""".stripMargin
    )
    assertNoDiff(FileLayout.asString(preferencesDirectory), "")
    assertNoDiff(
      FileLayout.asString(dataDirectory),
      """|/zsh-functions/_tests -> /data/zsh/_tests
         |/bash/tests.sh
         |# DO NOT EDIT: this script is automatically generated by the command 'tests completions install'.
         |_tests() {
         |  completions=$(tests completions run bash-v1 ${#COMP_WORDS[@]} ${COMP_WORDS[@]} 2> /dev/null)
         |  cur="${COMP_WORDS[COMP_CWORD]}"
         |  COMPREPLY=($(compgen -W "$completions" -- $cur))
         |  return 0
         |}
         |complete -F _tests tests
         |
         |/zsh/_tests
         |#compdef _tests tests
         |
         |# DO NOT EDIT: this script is automatically generated by the command 'tests completions install'.
         |function _tests {
         |    compadd -- $(tests completions run zsh-v1 $CURRENT $words[@] 2> /dev/null)
         |}
         |""".stripMargin
    )
    assertNoDiff(FileLayout.asString(cacheDirectory), "")

    // Assert that the 'uninstall' command cleans up the generated files.
    app.reset()
    assertEquals(app().run(List("completions", "uninstall")), 0)
    assertNoDiff(app.capturedOutput, "")
    assertNoDiff(FileLayout.asString(preferencesDirectory), "")
    assertNoDiff(FileLayout.asString(dataDirectory), "")
    assertNoDiff(
      FileLayout.asString(homeDirectory),
      """|/.config/fish/functions/example.fish
         |# Comment
         |
         |/.bashrc
         |# Comment
         |
         |/.zshrc
         |# Comment
         |""".stripMargin
    )
  }
}
