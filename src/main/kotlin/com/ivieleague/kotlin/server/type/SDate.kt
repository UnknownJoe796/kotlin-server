package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ivieleague.kotlin.server.type.meta.SPrimitiveClass
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

object SDate : SType<ZonedDateTime> {
    fun format() = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
    fun parseFormat() = DateTimeFormatterBuilder()
            // date/time
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // offset (hh:mm - "+00:00" when it's zero)
            .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
            // offset (hhmm - "+0000" when it's zero)
            .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
            // offset (hh - "Z" when it's zero)
            .optionalStart().appendOffset("+HH", "Z").optionalEnd()
            // create formatter
            .toFormatter()
    override val kclass = ZonedDateTime::class
    override fun parse(parser: JsonParser) = parseFormat().parse(parser.text, ZonedDateTime::from)
    override fun parse(node: JsonNode?) = if (node == null) default else parseFormat().parse(node.asText(), ZonedDateTime::from)
    override fun serialize(generator: JsonGenerator, value: ZonedDateTime) = generator.writeNullOr(value) {
        writeString(format().format(it))
    }

    override fun serialize(factory: JsonNodeFactory, value: ZonedDateTime): JsonNode = factory.nullNodeOr(value) { factory.textNode(format().format(it)) }

    override val name: String = "Date"
    override val description: String = "A date/time/timezone value."
    override fun reflect(): TypedObject = SPrimitiveClass.make(this)
    override val default: ZonedDateTime get() = ZonedDateTime.now()
}