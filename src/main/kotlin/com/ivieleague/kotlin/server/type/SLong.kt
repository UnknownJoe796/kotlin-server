package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SLong : SType<Long> {
    override val kclass = Long::class
    override fun parse(node: JsonNode?) = node?.asLong() ?: default
    override fun parse(parser: JsonParser) = parser.longValue
    override fun parse(parser: JsonParser, default: Long): Long {
        return if (parser.currentToken == JsonToken.VALUE_NUMBER_INT) parser.longValue
        else default
    }

    override fun parse(node: JsonNode?, default: Long): Long {
        return if (node == null) default
        else if (node.isInt) node.asLong()
        else default
    }
    override fun serialize(generator: JsonGenerator, value: Long) = generator.writeNumber(value)
    override fun serialize(factory: JsonNodeFactory, value: Long): JsonNode = factory.numberNode(value)
    override val name: String = "Long"
    override val description: String = "An integer value of 64 bits."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: Long = 0
}