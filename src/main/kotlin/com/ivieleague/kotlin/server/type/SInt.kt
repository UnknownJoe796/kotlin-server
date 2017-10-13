package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SInt : SType<Int> {
    override val kclass = Int::class
    override fun parse(node: JsonNode) = if (node.isNull) null else node.asInt()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.intValue
    override fun serialize(generator: JsonGenerator, value: Int?) = generator.writeNullOr(value) { writeNumber(it) }

    override fun toString() = "Int"
}