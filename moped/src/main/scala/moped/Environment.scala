package moped

import java.io.InputStream
import java.io.PrintStream

final case class Environment(
    standardOutput: PrintStream = System.out,
    standardError: PrintStream = System.err,
    standardInput: InputStream = System.in
)
