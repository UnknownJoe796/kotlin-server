package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SEnumClass
import java.util.*

interface SEnum : SType<SEnum.Value> {

    override val name: String
    override val description: String
    val values: Set<SEnum.Value>

    override val kclass get() = Byte::class

    data class Value(val name: String, val description: String)

    operator fun get(name: String): Value? = getNameIndex(this)[name]

    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else get(parser.text)
    override fun parse(node: JsonNode?): SEnum.Value? = if (node == null) null else if (node.isNull) null else get(node.asText())

    override fun reflect(): TypedObject = SEnumClass.make(this)

    override fun serialize(generator: JsonGenerator, value: SEnum.Value?) = generator.writeNullOr(value) {
        writeString(it.name)
    }

    override fun serialize(factory: JsonNodeFactory, value: SEnum.Value?): JsonNode = factory.nullNodeOr(value) { factory.textNode(it.name) }

    companion object {
        private val nameIndexes = WeakHashMap<SEnum, Map<String, Value>>()
        fun getNameIndex(enum: SEnum) = nameIndexes.getOrPut(enum) { enum.values.associateBy { it.name } }
    }
}