package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SFloat : SType<Float> {
    override val kclass = Float::class
    override fun parse(node: JsonNode?) = node?.asDouble()?.toFloat() ?: default
    override fun parse(parser: JsonParser) = parser.floatValue
    override fun parse(parser: JsonParser, default: Float): Float {
        return if (parser.currentToken == JsonToken.VALUE_NUMBER_FLOAT) parser.floatValue
        else if (parser.currentToken == JsonToken.VALUE_NUMBER_INT) parser.floatValue
        else default
    }

    override fun parse(node: JsonNode?, default: Float): Float {
        return if (node == null) default
        else if (node.isFloat) node.asDouble().toFloat()
        else default
    }
    override fun serialize(generator: JsonGenerator, value: Float) = generator.writeNumber(value)
    override fun serialize(factory: JsonNodeFactory, value: Float): JsonNode = factory.numberNode(value)
    override val name: String = "Float"
    override val description: String = "A single-precision floating point number."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: Float = 0f
}