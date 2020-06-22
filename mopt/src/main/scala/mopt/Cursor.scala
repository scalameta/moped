package mopt

sealed abstract class Cursor {
  var parent: Cursor = NoCursor()
  def copyThis(): Cursor =
    this match {
      case NoCursor()                => NoCursor()
      case SelectMemberCursor(value) => SelectMemberCursor(value)
      case SelectIndexCursor(value)  => SelectIndexCursor(value)
    }
}
final case class NoCursor() extends Cursor
final case class SelectMemberCursor(value: String) extends Cursor
final case class SelectIndexCursor(value: Int) extends Cursor
