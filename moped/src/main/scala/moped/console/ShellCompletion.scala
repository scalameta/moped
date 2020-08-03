package moped.console

sealed abstract class ShellCompletion
object ShellCompletion {
  def fromString(value: String): Option[ShellCompletion] =
    value match {
      case "zsh" => Some(ZshCompletion)
      case "bash" => Some(BashCompletion)
      case "fish" => Some(FishCompletion)
      case _ => None
    }
}

case object ZshCompletion extends ShellCompletion
case object BashCompletion extends ShellCompletion
case object FishCompletion extends ShellCompletion
