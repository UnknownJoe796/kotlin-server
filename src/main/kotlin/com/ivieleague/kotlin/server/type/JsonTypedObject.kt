package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.databind.JsonNode

class JsonTypedObject(override val type: SClass, val source: JsonNode) : MutableTypedObject {
    override fun <T : Any> get(field: SClass.Field<T>): T? = source.get(field.key)?.let { field.type.parse(it) }
    override fun <T : Any> set(field: SClass.Field<T>, value: T?) {
        field.type.serialize()
    }
}