package moped.console

import moped.macros.ParameterShape

final case class TabCompletionContext(
    format: Option[String],
    current: Option[Int],
    arguments: List[String],
    last: String,
    secondLast: Option[String],
    setting: Option[ParameterShape],
    allSettings: Map[String, ParameterShape],
    app: Application
)