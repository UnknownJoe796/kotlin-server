package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SBoolean : SType<Boolean> {
    override val kclass = Boolean::class
    override fun parse(parser: JsonParser): Boolean? = if (parser.currentToken == JsonToken.VALUE_NULL) null else parser.booleanValue
    override fun parse(node: JsonNode) = if (node.isNull) null else node.asBoolean()
    override fun serialize(generator: JsonGenerator, value: Boolean?) = generator.writeNullOr(value) { writeBoolean(it) }
    override fun toString() = "Boolean"
}