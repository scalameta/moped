package moped

import scala.collection.mutable

sealed abstract class Cursor {
  private var myParent: Cursor = NoCursor()
  def parent: Cursor = myParent
  def isEmpty: Boolean = this.isInstanceOf[NoCursor]
  def parents: List[Cursor] = {
    val buf = mutable.ListBuffer.empty[Cursor]
    def loop(c: Cursor): Unit =
      c.parent match {
        case NoCursor() =>
        case other =>
          loop(other.parent)
          buf += other
      }
    loop(this)
    buf.toList
  }
  def path: String = {
    parents.iterator.map(_.syntax).mkString(".")
  }
  def syntax: String =
    this match {
      case NoCursor()                => ""
      case SelectMemberCursor(value) => value
      case SelectIndexCursor(value)  => s"($value)"
    }
  def withParent(newParent: Cursor): Cursor = {
    val result = copyThis()
    result.myParent = newParent
    result
  }
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
