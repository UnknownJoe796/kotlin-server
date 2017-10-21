package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
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

        for ((key, item) in it.entries) {
            writeFieldName(key)

            val field = fields[key]
            if (item == null)
                writeNull()
            else {
                val type = (field?.type ?: SPrimitives.getDefault(it.javaClass)) as SType<Any>
                type.serialize(generator, item)
            }
        }
        writeEndObject()
    }

    override val dependencies: Collection<SType<*>>
        get() = implementers.values

    override fun reflect(user: TypedObject?): TypedObject = SInterfaceClass.make(this)
}