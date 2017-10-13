package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SLong : SType<Long> {
    override val kclass = Long::class
    override fun parse(node: JsonNode) = node.asLong()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.longValue
    override fun serialize(generator: JsonGenerator, value: Long?) = generator.writeNullOr(value) { writeNumber(it) }
    override fun toString() = "Long"
}