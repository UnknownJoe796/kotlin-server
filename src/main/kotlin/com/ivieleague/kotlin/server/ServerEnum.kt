package com.ivieleague.kotlin.server

/**
 * Created by josep on 6/8/2017.
 */
class ServerEnum(
        val name: String,
        val description: String,
        val values: Set<Value>
) {
    data class Value(val name: String, val description: String, val value: Int)
}