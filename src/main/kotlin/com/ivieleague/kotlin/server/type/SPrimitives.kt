package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SPrimitives {

    val types: List<SType<*>> = listOf<SType<*>>(
            SVoid,
            SBoolean,
            SInt,
            SLong,
            SFloat,
            SDouble,
            SString,
            SDate,
            SJson,
            STypedValue
    )

    private val mapped = types.associate { it.jclass to it }
    fun getDefault(clazz: Class<*>) = mapped[clazz] ?: throw IllegalArgumentException("No default for type ${javaClass.name}.")
    fun getDefault(node: JsonNode): SType<*> = when {
        node.isBoolean -> SBoolean
        node.isInt -> SInt
        node.isLong -> SLong
        node.isFloat -> SFloat
        node.isDouble -> SDouble
        node.isTextual -> SString
        node.isNull -> SVoid
        else -> throw IllegalArgumentException("No default for node.")
    }

    fun getDefault(token: JsonToken): SType<*> = when (token) {
        JsonToken.VALUE_STRING -> SString
        JsonToken.VALUE_NUMBER_INT -> SLong
        JsonToken.VALUE_NUMBER_FLOAT -> SDouble
        JsonToken.VALUE_FALSE -> SBoolean
        JsonToken.VALUE_TRUE -> SBoolean
        JsonToken.VALUE_NULL -> SVoid
        else -> throw IllegalArgumentException("No default for token $token.")
    }
}