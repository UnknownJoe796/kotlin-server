package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

class JsonTypedObject(override val type: SClass, val factory: JsonNodeFactory, val source: ObjectNode) : MutableTypedObject {
    override fun <T> get(field: TypeField<T>): T? = field.type.parse(source.get(field.key))
    override fun <T> set(field: TypeField<T>, value: T) {
        source.set(field.key, field.type.serialize(factory, value))
    }
}