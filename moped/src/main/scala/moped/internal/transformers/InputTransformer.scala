package moped.internal.transformers

import java.nio.CharBuffer

import moped.internal.diagnostics.DiagnosticException
import moped.reporters.Diagnostic
import moped.reporters.Input
import moped.reporters.RangePosition
import ujson._
import upickle.core.ObjArrVisitor
import upickle.core.Visitor

object InputTransformer extends Transformer[Input] {
  def transform[T](j: Input, f: Visitor[_, T]): T =
    new InputTransformer(j).parse(f)
}

final class InputTransformer[J](input: Input) extends CharParser[J] {
  var line = 0
  val chars = input.chars
  val wrapped: CharBuffer = CharBuffer.wrap(chars)

  override def readDataIntoBuffer(
      buffer: Array[Char],
      bufferOffset: Int
  ): (Array[Char], Boolean, Int) = {
    if (buffer == null)
      (chars, false, chars.length)
    else
      (chars, true, -1)
  }

  override def die(i: Int, msg: String): Nothing = {
    val pos = RangePosition(input, i, i)
    val error = pos.pretty("error", msg)
    throw new ParseException(error, i) {
      // super.getMessage appends useless "at index N" suffix.
      override def getMessage: String = error
    }
  }

  override def reject(j: Int): PartialFunction[Throwable, Nothing] = {
    case e: StringIndexOutOfBoundsException =>
      val n = chars.length - 1
      val pos = RangePosition(input, n, n)
      throw new DiagnosticException(Diagnostic.error("incomplete JSON", pos))
  }

  private def trailingComma(i: Int): Int =
    at(i) match {
      case ',' =>
        var curr = i + 1
        var done = false
        while (!atEof(curr) && !done) {
          at(curr) match {
            case '/' =>
              curr = comment(curr) + 1
            case ' ' | '\n' =>
              curr = curr + 1
            case _ =>
              done = true
          }
        }
        at(curr) match {
          case ']' | '}' =>
            curr
          case _ =>
            i
        }
      case _ =>
        i
    }

  private def comment(i: Int): Int =
    at(i) match {
      case '/' =>
        at(i + 1) match {
          case '/' =>
            var curr = i + 2
            while (!atEof(curr) && at(curr) != '\n') {
              curr += 1
            }
            curr
          case _ =>
            i
        }
      case _ =>
        i
    }

  def column(i: Int): Int = i
  def newline(i: Int): Unit = {
    line += 1
  }
  def reset(i: Int): Int = {
    if (atEof(i)) {
      i
    } else {
      val next =
        at(i) match {
          case '/' =>
            comment(i)
          case ',' =>
            trailingComma(i)
          case _ =>
            i
        }
      if (next == i)
        i
      else
        reset(next)
    }
  }

  def checkpoint(
      state: Int,
      i: Int,
      stack: List[ObjArrVisitor[_, J]],
      path: List[Any]
  ): Unit = ()

  def at(i: Int): Char = {
    if (i >= chars.length)
      throw new StringIndexOutOfBoundsException(i)
    chars(i)
  }
  override def atEof(i: Int): Boolean = i >= chars.length
  override def close(): Unit = ()
  override def dropBufferUntil(i: Int): Unit = ()
}
