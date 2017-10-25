package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SMap<T : Any> private constructor(val ofType: SType<T>) : SType<Map<String, T?>> {
    override val kclass = Map::class

    override fun parse(parser: JsonParser): Map<String, T?>? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = HashMap<String, T?>()
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            result[parser.currentName] = ofType.parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode): Map<String, T?>? {
        if (node.isNull) return null

        val result = HashMap<String, T?>()
        for ((key, value) in node.fields()) {
            result[key] = ofType.parse(value)
        }
        return result
    }

    override fun serialize(generator: JsonGenerator, value: Map<String, T?>?) = generator.writeNullOr(value) {
        writeStartObject()
        for ((key, item) in it) {
            writeFieldName(key)
            if (item == null)
                writeNull()
            else {
                ofType.serialize(generator, item)
            }
        }
        writeEndObject()
    }

    override val name: String = "Map<${ofType.name}>"
    override val description: String = "A map of strings to ${ofType.name}."

    override val dependencies: Collection<SType<*>>
        get() = listOf(ofType)

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)


    companion object {
        private val cache = HashMap<SType<*>, SMap<*>>()
        operator fun <T : Any> get(type: SType<T>) = cache.getOrPut(type) { SMap(type) } as SMap<T>
    }
}