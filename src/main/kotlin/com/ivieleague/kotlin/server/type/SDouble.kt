package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SDouble : SType<Double> {
    override val kclass = Double::class
    override fun parse(node: JsonNode) = if (node.isNull) null else node.asDouble()
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.doubleValue
    override fun serialize(generator: JsonGenerator, value: Double?) = generator.writeNullOr(value) { writeNumber(it) }
    override val name: String = "Double"
    override val description: String = "A double-precision floating point number."
    override fun reflect(user: TypedObject?): TypedObject = SPrimitiveClass.make(this)
}