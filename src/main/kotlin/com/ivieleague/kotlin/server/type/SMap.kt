package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SMap<T> private constructor(val ofType: SType<T>) : SType<Map<String, T>> {
    override val kclass = Map::class

    override fun parse(parser: JsonParser): Map<String, T> {

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = HashMap<String, T>()
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            result[parser.currentName] = ofType.parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode?): Map<String, T> {
        if (node == null) return default

        val result = HashMap<String, T>()
        for ((key, value) in node.fields()) {
            result[key] = ofType.parse(value)
        }
        return result
    }

    override fun parse(parser: JsonParser, default: Map<String, T>): Map<String, T> {

        assert(parser.currentToken == JsonToken.START_OBJECT)

        val result = HashMap<String, T>()
        var token = parser.nextValue()
        while (token != JsonToken.END_OBJECT) {
            try {
                result[parser.currentName] = ofType.parse(parser)
            } catch (e: Exception) {

            }
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode?, default: Map<String, T>): Map<String, T> {
        if (node == null) return default

        val result = HashMap<String, T>()
        for ((key, value) in node.fields()) {
            try {
                result[key] = ofType.parse(value)
            } catch (e: Exception) {

            }
        }
        return result
    }

    override fun serialize(generator: JsonGenerator, value: Map<String, T>) = generator.writeNullOr(value) {
        writeStartObject()
        for ((key, item) in it) {
            writeFieldName(key)
            ofType.serialize(generator, item)
        }
        writeEndObject()
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: Map<String, T>): JsonNode = factory.nullNodeOr(value) {
        objectNode().apply {
            for ((key, item) in it.entries) {
                set(key, ofType.serialize(factory, item))
            }
        }
    }

    override val name: String = "Map<${ofType.name}>"
    override val description: String = "A map of strings to ${ofType.name}."

    override val dependencies: Collection<SType<*>>
        get() = listOf(ofType)

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)

    override val default: Map<String, T> get() = mapOf()

    companion object {
        private val cache = HashMap<SType<*>, SMap<*>>()
        operator fun <T> get(type: SType<T>) = cache.getOrPut(type) { SMap(type) } as SMap<T>
    }
}