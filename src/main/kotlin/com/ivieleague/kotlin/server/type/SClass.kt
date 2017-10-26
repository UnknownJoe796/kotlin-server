package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.type.meta.SClassClass

interface SClass : SType<TypedObject> {
    override val name: String
    override val description: String
    val fields: Map<String, Field<*>>
    val primaryKey: List<Field<*>> get() = listOf()

    override val dependencies: Collection<SType<*>>
        get() = fields.map { it.value.type }

    override val kclass get() = Map::class

    override fun parse(node: JsonNode): TypedObject? = parseDirect(JsonGlobals.jsonNodeFactory, node)
    fun parseSimple(node: JsonNode): SimpleTypedObject? {
        if (node.isNull) return null
        val result = SimpleTypedObject(this)
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

    fun parseDirect(factory: JsonNodeFactory, node: JsonNode): JsonTypedObject? {
        return if (node.isNull) null
        else JsonTypedObject(this, factory, node as ObjectNode)
    }

    override fun parse(parser: JsonParser): TypedObject? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = SimpleTypedObject(this)
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
        for ((key, field) in fields) {
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
    override fun serialize(factory: JsonNodeFactory, value: TypedObject?): JsonNode {
        return if (value is JsonTypedObject) value.source
        else factory.nullNodeOr(value) {
            objectNode().apply {
                for ((key, field) in fields) {
                    val item = it[field]
                    if (item == null)
                        set(key, nullNode())
                    else {
                        set(key, (field.type as SType<Any>).serialize(factory, item))
                    }
                }
            }
        }
    }

    data class Field<T : Any>(
            val key: String,
            val description: String,
            val type: SType<T>,
            val default: T? = null
    )

    override fun reflect(): TypedObject = SClassClass.make(this)
}