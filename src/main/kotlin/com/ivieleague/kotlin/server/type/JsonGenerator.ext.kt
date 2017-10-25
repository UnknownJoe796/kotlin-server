package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonGenerator

inline fun <T> JsonGenerator.writeNullOr(value: T?, action: JsonGenerator.(T) -> Unit) {
    if (value == null)
        writeNull()
    else
        action(this, value)
}

