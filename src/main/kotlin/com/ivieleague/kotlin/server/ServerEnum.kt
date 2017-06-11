package com.ivieleague.kotlin.server

/**
 * Created by josep on 6/8/2017.
 */
class ServerEnum(
        val name: String,
        val description: String,
        val values: Set<Value>
) {
    data class Value(val name: String, val description: String, val value: Byte)

    val indexedByValue: Map<Byte, Value> = values.associateBy { it.value }
    val indexedByName: Map<String, Value> = values.associateBy { it.name }

    operator fun get(byte: Byte): Value = indexedByValue[byte]!!
    operator fun get(name: String): Value = indexedByName[name]!!
}