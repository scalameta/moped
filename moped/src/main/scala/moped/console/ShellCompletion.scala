package moped.console

sealed abstract class ShellCompletion

case object ZshCompletion extends ShellCompletion
case object BashCompletion extends ShellCompletion
case object FishCompletion extends ShellCompletion
