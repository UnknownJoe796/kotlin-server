package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SUntypedList : SType<List<Any?>> {
    override val kclass = List::class
    override fun parse(node: JsonNode): List<Any?>? {
        if (node.isNull) return null

        val result = ArrayList<Any?>()
        for (value in node.elements()) {
            result += SPrimitives.getDefault(value).parse(value)
        }
        return result
    }

    override fun parse(parser: JsonParser): List<Any?>? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_ARRAY)

        val result = ArrayList<Any?>()
        var token = parser.nextValue()
        while (token != JsonToken.END_ARRAY) {
            result += SPrimitives.getDefault(token).parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(generator: JsonGenerator, value: List<Any?>?) = generator.writeNullOr(value) {
        writeStartArray()
        for (item in it) {
            if (item == null)
                writeNull()
            else {
                val type = SPrimitives.getDefault(it.javaClass) as SType<Any>
                type.serialize(generator, item)
            }
        }
        writeEndArray()
    }

    override fun toString() = "List"
}