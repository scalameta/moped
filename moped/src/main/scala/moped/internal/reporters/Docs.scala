package moped.internal.reporters

import org.typelevel.paiges.Doc

object Docs {
  val blankLine: Doc = Doc.line + Doc.line
  val quote: Doc = Doc.char('"')
  def quoted(d: String): Doc = quote + Doc.text(d) + quote
  val IP: Doc = Doc.text(".IP ")
  val TH: Doc = Doc.text(".TH ")
  val SH: Doc = Doc.text(".SH ")
  val PP: Doc = Doc.text(".PP ")
  val RS: Doc = Doc.line + Doc.text(".RS 4 ") + Doc.line
  val RE: Doc = Doc.text(".RE ")

  val error: Doc = Doc.text("❗")
  def successMessage(message: String): Doc = success + Doc.text(message)
  val bold: Doc = Doc.zeroWidth(Console.BOLD)
  val green: Doc = Doc.zeroWidth(Console.GREEN)
  val reset: Doc = Doc.zeroWidth(Console.RESET)
  val success: Doc = green + Doc.text("✔ ") + reset
}
