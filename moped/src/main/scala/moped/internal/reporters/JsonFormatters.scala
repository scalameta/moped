package moped.internal.reporters

object JsonFormatters {
  def escape(str: String): String = {
    val out = new java.lang.StringBuilder()
    var i = 0
    while (i < str.length()) {
      out.append(
        str.charAt(i) match {
          case '\\' =>
            "\\\\"
          case '\n' =>
            "\\n"
          case '"' =>
            "\""
          case other =>
            other
        }
      )
      i += 1
    }
    out.toString()
  }
}
