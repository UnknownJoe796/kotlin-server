package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

class SList<T : Any>(val ofType: SType<T>) : SType<List<T?>> {
    override val kclass = List::class

    override fun parse(parser: JsonParser): List<T?>? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_ARRAY)

        val result = ArrayList<T?>()
        var token = parser.nextValue()
        while (token != JsonToken.END_ARRAY) {
            result += ofType.parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode): List<T?>? {
        if (node.isNull) return null

        val result = ArrayList<T?>()
        for (value in node.elements()) {
            result += ofType.parse(value)
        }
        return result
    }

    override fun serialize(generator: JsonGenerator, value: List<T?>?) = generator.writeNullOr(value) {
        writeStartArray()
        for (item in it) {
            if (item == null)
                writeNull()
            else {
                ofType.serialize(generator, item)
            }
        }
        writeEndArray()
    }

    override fun toString() = "List<$ofType>"
}