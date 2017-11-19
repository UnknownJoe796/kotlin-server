package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SNullable
import com.ivieleague.kotlin.server.type.SPointer
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypeField

object IdField {
    val cache = HashMap<SType<*>, TypeField<String?>>()
    operator fun get(type: SType<*>) = cache.getOrPut(type) {
        TypeField(
                key = "id",
                description = "The ID of the object.",
                type = SNullable[SPointer[type]]
        )
    }
}