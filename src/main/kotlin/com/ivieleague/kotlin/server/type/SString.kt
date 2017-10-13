package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SString : SType<String> {
    override val kclass = String::class
    override fun parse(node: JsonNode) = node.asText()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.text
    override fun serialize(generator: JsonGenerator, value: String?) = generator.writeNullOr(value) { writeString(it) }
    override fun toString() = "String"
}