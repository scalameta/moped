package moped.progressbars

import org.typelevel.paiges.Doc

final case class ProgressStep(
    static: Doc = Doc.empty,
    dynamic: Doc = Doc.empty
)

object ProgressStep {
  val empty: ProgressStep = ProgressStep(Doc.empty, Doc.empty)
}
