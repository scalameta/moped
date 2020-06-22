package mopt

import fansi.Attr
import fansi.Color

sealed abstract class Severity(val name: String, val priority: Int)
    extends Ordered[Severity] {
  def compare(that: Severity): Int = {
    Integer.compare(this.priority, that.priority)
  }
}
object Severity {
  def fromPriority(n: Int): Severity =
    n match {
      case 0     => SilentSeverity
      case 1     => DebugSeverity
      case 2     => InfoSeverity
      case 3     => WarningSeverity
      case 4     => ErrorSeverity
      case other => throw new IndexOutOfBoundsException(other.toString())
    }
  val all = 0.to(4).toList.map(fromPriority)
  def color(sev: Severity): Attr =
    sev match {
      case SilentSeverity  => Color.LightMagenta
      case DebugSeverity   => Color.LightGreen
      case InfoSeverity    => Color.LightBlue
      case WarningSeverity => Color.LightYellow
      case ErrorSeverity   => Color.LightRed
    }
}
case object SilentSeverity extends Severity("silent", 0)
case object DebugSeverity extends Severity("debug", 1)
case object InfoSeverity extends Severity("info", 2)
case object WarningSeverity extends Severity("warning", 3)
case object ErrorSeverity extends Severity("error", 4)
