package moped.progressbars

import dataclass.data
import org.typelevel.paiges.Doc

@data
class ProgressStep(static: Doc = Doc.empty, dynamic: Doc = Doc.empty)

object ProgressStep {
  val empty: ProgressStep = ProgressStep(Doc.empty, Doc.empty)
}
