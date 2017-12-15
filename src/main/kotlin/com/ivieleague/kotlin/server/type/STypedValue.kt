package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object STypedValue : SType<TypedValue<*>> {
    override val kclass = Any::class
    override fun parse(node: JsonNode?): TypedValue<*> {
        val typeName = node!!["type"].asText()
        val valueNode = node["value"]
        @Suppress("UNCHECKED_CAST")
        val type = CentralTypeRegistry[typeName] as SType<Any>
        return TypedValue(type, type.parse(node))
    }

    override fun serialize(generator: JsonGenerator, value: TypedValue<*>) = value.serialize(generator)
    override fun serialize(factory: JsonNodeFactory, value: TypedValue<*>): JsonNode = value.serialize(factory)
    override val name: String = "TypedValue"
    override val description: String = "A holder that can hold an object of any type."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: TypedValue<*> = TypedValue(SVoid, Unit)

}

data class TypedValue<T>(val type: SType<T>, val value: T) {
    //TODO Registry based on any's that are created.  This should change.
    init {
        CentralTypeRegistry[type.name] = type
    }

    fun serialize(generator: JsonGenerator) {
        generator.writeStartObject()
        generator.writeFieldName("type")
        generator.writeString(type.name)
        generator.writeFieldName("value")
        type.serialize(generator, value)
        generator.writeEndObject()
    }

    fun serialize(factory: JsonNodeFactory): JsonNode {
        return factory.objectNode().apply {
            this["type"] = factory.textNode(type.name)
            this["value"] = type.serialize(factory, value)
        }
    }
}