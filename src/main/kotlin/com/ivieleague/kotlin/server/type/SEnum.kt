package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

class SEnum(
        val name: String,
        val description: String,
        val values: Set<SEnum.Value>
) : SType<SEnum.Value> {
    override val kclass = Byte::class

    data class Value(val name: String, val description: String, val value: Byte)

    val indexedByValue: Map<Byte, Value> = values.associateBy { it.value }
    val indexedByName: Map<String, Value> = values.associateBy { it.name }

    operator fun get(byte: Byte): Value = indexedByValue[byte]!!
    operator fun get(name: String): Value = indexedByName[name]!!

    override fun parse(parser: JsonParser) = if (parser.currentToken == JsonToken.VALUE_NULL) null else indexedByName[parser.text]
    override fun parse(node: JsonNode): SEnum.Value? = if (node.isNull) null else indexedByName[node.asText()]

    override fun serialize(generator: JsonGenerator, value: SEnum.Value?) = generator.writeNullOr(value) {
        writeString(it.name)
    }

    override fun toString(): String = name
}