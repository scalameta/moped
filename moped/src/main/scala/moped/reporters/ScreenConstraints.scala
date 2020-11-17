package moped.reporters

import dataclass.data

@data
class ScreenConstraints(
    minWidth: Int,
    maxWidth: Int,
    minHeight: Int,
    maxHeight: Int
)
