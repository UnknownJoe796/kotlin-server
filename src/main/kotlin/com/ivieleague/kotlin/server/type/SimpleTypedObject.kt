package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator

interface TypedObject {
    val type: SClass

    operator fun <T> get(field: TypeField<T>): T

    fun serialize(generator: JsonGenerator) = type.serialize(generator, this)
}

interface MutableTypedObject : TypedObject {
    operator fun <T> set(field: TypeField<T>, value: T)
}

class SimpleTypedObject(override val type: SClass) : HashMap<String, Any?>(), MutableTypedObject {

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(field: TypeField<T>): T {
        if(this.containsKey(field.key)) {
            return this[field.key] as T
        } else return field.defaultGenerator()
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> set(field: TypeField<T>, value: T) {
        this[field.key] = value
    }
}