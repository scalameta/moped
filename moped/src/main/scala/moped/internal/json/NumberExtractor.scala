package moped.internal.json

import scala.util.Try

object NumberExtractor {
  def unapply(arg: String): Option[Double] =
    Try(java.lang.Double.parseDouble(arg)).toOption
}
