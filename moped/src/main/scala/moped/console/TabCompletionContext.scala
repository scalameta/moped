package moped.console

import moped.generic.ParameterDefinition

final case class TabCompletionContext(
    format: Option[String],
    current: Option[Int],
    arguments: List[String],
    last: String,
    secondLast: Option[String],
    setting: Option[ParameterDefinition],
    allSettings: Map[String, ParameterDefinition],
    app: Application
)
