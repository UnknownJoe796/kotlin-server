package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SString
import com.ivieleague.kotlin.server.type.TypeField


val IdField = TypeField(
        key = "id",
        description = "The ID of this object in the database",
        type = SString,
        default = null
)