package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass

class Sort(
        val ascending: Boolean,
        val field: SClass.Field<*>
)