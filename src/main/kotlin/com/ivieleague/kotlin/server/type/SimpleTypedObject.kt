package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator

interface TypedObject {
    val type: SClass

    operator fun <T : Any> get(field: SClass.Field<T>): T?

    fun serialize(generator: JsonGenerator) = type.serialize(generator, this)
}

interface MutableTypedObject : TypedObject {
    operator fun <T : Any> set(field: SClass.Field<T>, value: T?)
}

class SimpleTypedObject(override val type: SClass) : HashMap<String, Any?>(), MutableTypedObject {

    @Suppress("UNCHECKED_CAST")
    override operator fun <T : Any> get(field: SClass.Field<T>): T? {
        return this[field.key] as T
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T : Any> set(field: SClass.Field<T>, value: T?) {
        this[field.key] = value
    }
}