package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SUntypedMap : SType<Map<String, Any?>> {
    override val kclass = Map::class
    override fun parse(node: JsonNode?): Map<String, Any?>? {
        if (node == null) return null
        if (node.isNull) return null

        val result = HashMap<String, Any?>()
        for ((key, value) in node.fields()) {
            result[key] = SPrimitives.getDefault(value).parse(value)
        }
        return result
    }

    override fun parse(parser: JsonParser): Map<String, Any?>? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = HashMap<String, Any?>()
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            result[parser.currentName] = SPrimitives.getDefault(token).parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(generator: JsonGenerator, value: Map<String, Any?>?) = generator.writeNullOr(value) {
        writeStartObject()
        for ((key, item) in it.entries) {
            writeFieldName(key)
            if (item == null)
                writeNull()
            else {
                val type = SPrimitives.getDefault(it.javaClass) as SType<Any>
                type.serialize(generator, item)
            }
        }
        writeEndObject()
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: Map<String, Any?>?): JsonNode = factory.nullNodeOr(value) {
        objectNode().apply {
            for ((key, item) in it.entries) {
                if (item == null)
                    set(key, nullNode())
                else {
                    val type = SPrimitives.getDefault(it.javaClass) as SType<Any>
                    set(key, type.serialize(factory, item))
                }
            }
        }
    }

    override val name: String = "Map"
    override val description: String = "An untyped map."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}