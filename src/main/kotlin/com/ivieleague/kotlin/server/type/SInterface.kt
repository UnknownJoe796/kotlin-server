package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SInterfaceClass

interface SInterface : SType<TypedObject> {
    override val name: String
    override val description: String
    val fields: Map<String, SClass.Field<*>>
    val implementers: Map<String, SClass>

    override val kclass get() = Map::class
    override fun parse(node: JsonNode): TypedObject? {
        if (node.isNull) return null
        val typeString = node.get("@type").asText()
        val sclass = implementers[typeString] ?: throw IllegalArgumentException("Type $typeString not found as an implementer of $name")
        return sclass.parse(node)
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(generator: JsonGenerator, value: TypedObject?) = generator.writeNullOr(value) {
        writeStartObject()

        writeFieldName("@type")
        writeString(it.type.name)

        for ((key, field) in it.type.fields) {
            writeFieldName(key)

            val item: Any? = it[field]
            if (item == null)
                writeNull()
            else {
                (field.type as SType<Any>).serialize(generator, item)
            }
        }
        writeEndObject()
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: TypedObject?) = factory.nullNodeOr(value) {
        writeStartObject()

        writeFieldName("@type")
        writeString(it.type.name)

        for ((key, field) in it.type.fields) {
            writeFieldName(key)

            val item: Any? = it[field]
            if (item == null)
                writeNull()
            else {
                (field.type as SType<Any>).serialize(generator, item)
            }
        }
        writeEndObject()
    }

    override val dependencies: Collection<SType<*>>
        get() = implementers.values

    override fun reflect(): TypedObject = SInterfaceClass.make(this)
}