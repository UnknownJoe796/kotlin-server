package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SString : SType<String> {
    override val kclass = String::class
    override fun parse(node: JsonNode?) = if (node == null) null else node.asText()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.text
    override fun serialize(generator: JsonGenerator, value: String?) = generator.writeNullOr(value) { writeString(it) }
    override fun serialize(factory: JsonNodeFactory, value: String?): JsonNode = factory.nullNodeOr(value) { factory.textNode(it) }
    override val name: String = "String"
    override val description: String = "A series of characters.  UTF-8 expected."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}