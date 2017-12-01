package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SDouble : SType<Double> {
    override val kclass = Double::class
    override fun parse(node: JsonNode?):Double = node?.asDouble() ?: default
    override fun parse(parser: JsonParser) = parser.doubleValue
    override fun serialize(generator: JsonGenerator, value: Double) = generator.writeNumber(value)
    override fun serialize(factory: JsonNodeFactory, value: Double): JsonNode = factory.numberNode(value)
    override val name: String = "Double"
    override val description: String = "A double-precision floating point number."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: Double = 0.0
}