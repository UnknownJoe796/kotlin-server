package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

inline fun <T> JsonGenerator.writeNullOr(value: T?, action: JsonGenerator.(T) -> Unit) {
    if (value == null)
        writeNull()
    else
        action(this, value)
}

inline fun <T> JsonNodeFactory.nullNodeOr(value: T?, action: JsonNodeFactory.(T) -> JsonNode): JsonNode {
    return if (value == null)
        this.nullNode()
    else
        action(this, value)
}