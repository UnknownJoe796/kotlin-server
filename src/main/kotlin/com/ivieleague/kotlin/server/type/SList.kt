package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SList<T : Any> private constructor(val ofType: SType<T>) : SType<List<T?>> {
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

    override fun parse(node: JsonNode?): List<T?>? {
        if (node == null) return null
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

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: List<T?>?) = factory.nullNodeOr(value) {
        arrayNode().apply {
            for (item in it) {
                if (item == null)
                    add(nullNode())
                else {
                    add(ofType.serialize(factory, item as? T))
                }
            }
        }
    }

    override val name: String = "List<${ofType.name}>"
    override val description: String = "A list of ${ofType.name}."

    override val dependencies: Collection<SType<*>>
        get() = listOf(ofType)

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)

    companion object {
        private val cache = HashMap<SType<*>, SList<*>>()
        operator fun <T : Any> get(type: SType<T>) = cache.getOrPut(type) { SList(type) } as SList<T>
    }
}