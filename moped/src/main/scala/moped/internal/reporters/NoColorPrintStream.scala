// This file contains adapted source code from tut, see NOTICE.md for LICENSE.
// Original source: https://github.com/tpolecat/tut/blob/e692c74afe7cb9f144f464b97f100c11367c7dfa/modules/core/src/main/scala/tut/AnsiFilterStream.scala
package moped.internal.reporters

import java.io.OutputStream
import java.io.PrintStream

class NoColorPrintStream(underlying: PrintStream)
    extends PrintStream(new NoColorOutputStream(underlying)) {
  def this(out: OutputStream) = this(new PrintStream(out))
}
