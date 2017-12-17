package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SList<T> private constructor(val ofType: SType<T>) : SType<List<T>> {
    override val kclass = List::class

    override fun parse(parser: JsonParser): List<T> {
        assert(parser.currentToken == JsonToken.START_ARRAY)

        val result = ArrayList<T>()
        var token = parser.nextValue()
        while (token != JsonToken.END_ARRAY) {
            result += ofType.parse(parser)
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode?): List<T> {
        if (node == null) return default

        val result = ArrayList<T>()
        for (value in node.elements()) {
            result += ofType.parse(value)
        }
        return result
    }

    override fun parse(parser: JsonParser, default: List<T>): List<T> {
        assert(parser.currentToken == JsonToken.START_ARRAY)

        val result = ArrayList<T>()
        var token = parser.nextValue()
        while (token != JsonToken.END_ARRAY) {
            try {
                result += ofType.parse(parser)
            } catch (e: Exception) {/*squish*/
            }
            token = parser.nextValue()
        }
        return result
    }

    override fun parse(node: JsonNode?, default: List<T>): List<T> {
        if (node == null) return default

        val result = ArrayList<T>()
        for (value in node.elements()) {
            try {
                result += ofType.parse(value)
            } catch (e: Exception) {/*squish*/
            }
        }
        return result
    }

    override fun serialize(generator: JsonGenerator, value: List<T>) {
        generator.apply {
            writeStartArray()
            for (item in value) {
                ofType.serialize(generator, item)
            }
            writeEndArray()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: List<T>) = factory.arrayNode().apply {
        for (item in value) {
            add(ofType.serialize(factory, item))
        }
    }


    override val name: String = "List<${ofType.name}>"
    override val description: String = "A list of ${ofType.name}."

    override val dependencies: Collection<SType<*>>
        get() = listOf(ofType)

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: List<T>
        get() = listOf()

    companion object {
        private val cache = HashMap<SType<*>, SList<*>>()
        operator fun <T> get(type: SType<T>) = cache.getOrPut(type) { SList(type) } as SList<T>
    }
}