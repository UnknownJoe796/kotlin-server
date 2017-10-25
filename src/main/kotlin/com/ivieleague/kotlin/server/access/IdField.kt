package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SString


val IdField = SClass.Field(
        key = "id",
        description = "The ID of this object in the database",
        type = SString,
        default = null
)