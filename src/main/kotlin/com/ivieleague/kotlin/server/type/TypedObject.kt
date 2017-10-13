package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator

class TypedObject(val type: SClass) : HashMap<String, Any?>() {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(field: SClass.Field<T>): T {
        return this[field.key] as T
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> set(field: SClass.Field<T>, value: T) {
        this[field.key] = value
    }

    fun serialize(generator: JsonGenerator) = type.serialize(generator, this)
}