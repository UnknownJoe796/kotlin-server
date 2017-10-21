package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.meta.SClassClass

interface SClass : SType<TypedObject> {
    override val name: String
    override val description: String
    val fields: Map<String, Field<*>>
    val primaryKey: List<Field<*>> get() = listOf()

    override val dependencies: Collection<SType<*>>
        get() = fields.map { it.value.type }

    override val kclass get() = Map::class
    override fun parse(node: JsonNode): TypedObject? {
        if (node.isNull) return null
        val result = TypedObject(this)
        for ((key, value) in node.fields()) {
            val field = fields[key]
            if (field == null) {
                result[key] = SPrimitives.getDefault(value).parse(value)
            } else {
                result[key] = field.type.parse(value)
            }
        }
        return result
    }

    override fun parse(parser: JsonParser): TypedObject? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = TypedObject(this)
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            val key = parser.currentName
            val field = fields[key]
            if (field == null) {
                result[key] = SPrimitives.getDefault(token).parse(parser)
            } else {
                result[key] = field.type.parse(parser)
            }
            token = parser.nextValue()
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(generator: JsonGenerator, value: TypedObject?) = generator.writeNullOr(value) {
        writeStartObject()
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

    data class Field<T : Any>(
            val key: String,
            val description: String,
            val type: SType<T>,
            val default: T? = null
    )

    override fun reflect(user: TypedObject?): TypedObject = SClassClass.make(this)
}