package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SFloat : SType<Float> {
    override val kclass = Float::class
    override fun parse(node: JsonNode?) = if (node == null) null else if (node.isNull) null else node.asDouble().toFloat()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.floatValue
    override fun serialize(generator: JsonGenerator, value: Float?) = generator.writeNullOr(value) { writeNumber(it) }
    override fun serialize(factory: JsonNodeFactory, value: Float?): JsonNode = factory.nullNodeOr(value) { factory.numberNode(it) }
    override val name: String = "Float"
    override val description: String = "A single-precision floating point number."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}