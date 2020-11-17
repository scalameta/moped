package moped.internal.console

import dataclass.data
import moped.macros.ParameterShape

@data
class InlinedFlag(keys: List[String], shape: ParameterShape)

object InlinedFlag {
  def apply(param: ParameterShape): InlinedFlag =
    InlinedFlag(param.name :: Nil, param)
}
