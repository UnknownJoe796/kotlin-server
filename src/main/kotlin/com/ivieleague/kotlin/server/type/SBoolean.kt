package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SBoolean : SType<Boolean> {
    override val kclass = Boolean::class
    override fun parse(parser: JsonParser): Boolean = parser.booleanValue
    override fun parse(node: JsonNode?) = node?.asBoolean() ?: default
    override fun parse(parser: JsonParser, default: Boolean): Boolean {
        return if (parser.currentToken == JsonToken.VALUE_TRUE) true
        else if (parser.currentToken == JsonToken.VALUE_FALSE) false
        else default
    }

    override fun parse(node: JsonNode?, default: Boolean): Boolean {
        return if (node == null) default
        else if (node.isBoolean) node.asBoolean()
        else default
    }


    override fun serialize(generator: JsonGenerator, value: Boolean) = generator.writeBoolean(value)
    override fun serialize(factory: JsonNodeFactory, value: Boolean): JsonNode = factory.booleanNode(value)
    override val name: String = "Boolean"
    override val description: String = "A boolean value, which can be either true or false."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: Boolean = false
}