package moped.internal.reporters

import java.io.OutputStream

import scala.collection.mutable.ArrayBuffer

class NoColorOutputStream(underlying: OutputStream) extends OutputStream() {

  private val stack = ArrayBuffer.empty[Int]
  private var state = AnsiStateMachine.Start

  override def write(ch: Int): Unit =
    synchronized {
      state.apply(ch) match {
        case AnsiStateMachine.Print =>
          stack.foreach { i =>
            underlying.write(i)
          }
          underlying.write(ch)
          resetState()
        case AnsiStateMachine.Discard =>
          resetState()
        case AnsiStateMachine.LineFeed =>
          flush()
          resetState()
        case other =>
          stack += ch
          state = other
      }
    }

  private def resetState(): Unit = {
    stack.clear()
    state = AnsiStateMachine.Start
  }

}
