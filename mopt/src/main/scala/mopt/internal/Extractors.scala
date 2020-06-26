package mopt.internal

import mopt.JsonString
import scala.util.Try

object Extractors {
  object Number {
    def unapply(arg: String): Option[BigDecimal] =
      Try(BigDecimal(arg)).toOption
  }
  object NestedKey {
    def unapply(arg: JsonString): Option[(String, String)] = {
      unapply(arg.value)
    }
    def unapply(arg: String): Option[(String, String)] = {
      val idx = arg.indexOf('.')
      if (idx == -1) None
      else {
        arg.splitAt(idx) match {
          case (_, "") => None
          case (a, b)  => Some(a -> b.stripPrefix("."))
        }
      }
    }
  }
}
