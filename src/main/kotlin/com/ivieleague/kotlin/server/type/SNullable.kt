package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

class SNullable<T> private constructor(val ofType: SType<T>) : SType<T?> {
    override val kclass = ofType.kclass

    override fun parse(parser: JsonParser): T? {
        if (parser.currentToken == JsonToken.VALUE_NULL) return null
        return ofType.parse(parser)
    }

    override fun parse(node: JsonNode?): T? {
        if (node == null) return null
        if (node.isNull) return null
        return ofType.parse(node)
    }

    override fun serialize(generator: JsonGenerator, value: T?) = generator.writeNullOr(value) {
        ofType.serialize(generator, it)
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(factory: JsonNodeFactory, value: T?) = if (value == null)
        factory.nullNode()
    else
        ofType.serialize(factory, value)

    override val name: String = "Nullable<${ofType.name}>"
    override val description: String = "A value that could be null or of type ${ofType.name}."

    override val dependencies: Collection<SType<*>>
        get() = listOf(ofType)

    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: T? = null

    companion object {
        private val cache = HashMap<SType<*>, SNullable<*>>()
        operator fun <T> get(type: SType<T>) = cache.getOrPut(type) { SNullable(type) } as SNullable<T>
    }
}