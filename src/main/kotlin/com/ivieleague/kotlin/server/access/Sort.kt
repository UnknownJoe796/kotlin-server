package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.TypeField

class Sort(
        val ascending: Boolean,
        val field: TypeField<*>
)
