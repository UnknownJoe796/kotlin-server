package com.ivieleague.kotlin.server.model

/**
 * Created by josep on 6/8/2017.
 */
class ServerEnum(
        val name: String,
        val description: String,
        val values: Set<ServerEnum.Value>
) {
    data class Value(val name: String, val description: String, val value: Byte)

    val indexedByValue: Map<Byte, ServerEnum.Value> = values.associateBy { it.value }
    val indexedByName: Map<String, ServerEnum.Value> = values.associateBy { it.name }

    operator fun get(byte: Byte): ServerEnum.Value = indexedByValue[byte]!!
    operator fun get(name: String): ServerEnum.Value = indexedByName[name]!!
}