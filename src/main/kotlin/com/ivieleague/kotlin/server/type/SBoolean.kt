package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass

object SBoolean : SType<Boolean> {
    override val kclass = Boolean::class
    override fun parse(parser: JsonParser): Boolean? = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.booleanValue
    override fun parse(node: JsonNode) = if (node.isNull) null else node.asBoolean()
    override fun serialize(generator: JsonGenerator, value: Boolean?) = generator.writeNullOr(value) { writeBoolean(it) }
    override val name: String = "Boolean"
    override val description: String = "A boolean value, which can be either true or false."
    override fun reflect(user: TypedObject?): TypedObject = SPrimitiveClass.make(this)
}