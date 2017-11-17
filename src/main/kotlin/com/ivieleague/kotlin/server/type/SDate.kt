package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass
import java.time.ZonedDateTime

object SDate : SType<ZonedDateTime> {
    val format = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
    override val kclass = ZonedDateTime::class
    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else format.parse(parser.text, ZonedDateTime::from)
    override fun parse(node: JsonNode) = if (node.isNull) null else format.parse(node.asText(), ZonedDateTime::from)
    override fun serialize(generator: JsonGenerator, value: ZonedDateTime?) = generator.writeNullOr(value) {
        writeString(format.format(it))
    }

    override fun serialize(factory: JsonNodeFactory, value: ZonedDateTime?): JsonNode = factory.nullNodeOr(value) { factory.textNode(format.format(it)) }

    override val name: String = "Date"
    override val description: String = "A date/time/timezone value."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
}