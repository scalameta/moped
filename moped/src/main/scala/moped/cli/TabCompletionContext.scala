package moped.cli

import dataclass.data
import moped.internal.console.ValidOption
import moped.macros.ParameterShape

@data
class TabCompletionContext(
    shell: ShellCompletion,
    arguments: List[String],
    last: String,
    secondLast: Option[String],
    setting: Option[ParameterShape],
    allSettings: Map[String, List[ValidOption]],
    app: Application
)
