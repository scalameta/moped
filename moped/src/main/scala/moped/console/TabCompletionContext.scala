package moped.console

import moped.macros.ParameterShape

final case class TabCompletionContext(
    shell: CompletionShell,
    current: Int,
    arguments: List[String],
    last: String,
    secondLast: Option[String],
    setting: Option[ParameterShape],
    allSettings: Map[String, ParameterShape],
    app: Application
)
