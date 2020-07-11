package moped.internal.json

import moped.json.JsonString
import scala.util.Try

object NestedJsonKey {
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
