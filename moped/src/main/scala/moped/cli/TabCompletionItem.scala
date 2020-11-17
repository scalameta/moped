package moped.cli

import dataclass.data

@data
class TabCompletionItem(name: String, description: String = "")
