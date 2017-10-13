package com.ivieleague.kotlin.server.type

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object SPrimitives {

    val types: List<SType<*>> = listOf<SType<*>>(
            SBoolean,
            SInt,
            SLong,
            SFloat,
            SDouble,
            SString,
            SUntypedList,
            SUntypedMap,
            SDate
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
        node.isObject -> SUntypedMap
        node.isArray -> SUntypedList
        else -> throw IllegalArgumentException("No default for node.")
    }

    fun getDefault(token: JsonToken): SType<*> = when (token) {
        JsonToken.START_OBJECT -> SUntypedMap
        JsonToken.START_ARRAY -> SUntypedList
        JsonToken.VALUE_STRING -> SString
        JsonToken.VALUE_NUMBER_INT -> SLong
        JsonToken.VALUE_NUMBER_FLOAT -> SDouble
        JsonToken.VALUE_FALSE -> SBoolean
        JsonToken.VALUE_TRUE -> SBoolean
        else -> throw IllegalArgumentException("No default for token $token.")
    }
}