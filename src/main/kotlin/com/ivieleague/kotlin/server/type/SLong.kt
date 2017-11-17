package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SLong : SType<Long> {
    override val kclass = Long::class
    override fun parse(node: JsonNode) = node.asLong()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.longValue
    override fun serialize(generator: JsonGenerator, value: Long?) = generator.writeNullOr(value) { writeNumber(it) }
    override fun serialize(factory: JsonNodeFactory, value: Long?): JsonNode = factory.nullNodeOr(value) { factory.numberNode(it) }
    override val name: String = "Long"
    override val description: String = "An integer value of 64 bits."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}