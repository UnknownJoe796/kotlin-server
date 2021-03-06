package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SString : SType<String> {
    override val kclass = String::class
    override fun parse(node: JsonNode?) = node?.asText() ?: default
    override fun parse(parser: JsonParser) = parser.text
    override fun parse(parser: JsonParser, default: String): String {
        return if (parser.currentToken == JsonToken.VALUE_STRING) parser.valueAsString
        else default
    }

    override fun parse(node: JsonNode?, default: String): String {
        return if (node == null) default
        else if (node.isTextual) node.textValue()
        else default
    }
    override fun serialize(generator: JsonGenerator, value: String) = generator.writeString(value)
    override fun serialize(factory: JsonNodeFactory, value: String): JsonNode = factory.textNode(value)
    override val name: String = "String"
    override val description: String = "A series of characters.  UTF-8 expected."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: String = ""
}