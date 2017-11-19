package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SInt : SType<Int> {
    override val kclass = Int::class
    override fun parse(node: JsonNode?) = node!!.asInt()
    override fun parse(parser: JsonParser) = parser.intValue
    override fun serialize(generator: JsonGenerator, value: Int) = generator.writeNumber(value)
    override fun serialize(factory: JsonNodeFactory, value: Int): JsonNode = factory.numberNode(value)
    override val name: String = "Int"
    override val description: String = "An integer value of 32 bits."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}