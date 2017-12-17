package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.generateString
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SType
import java.io.File

class JsonFileSingletonAccess<T>(
        override val type: SType<T>,
        val file: File,
        val default: T
) : SingletonAccess<T> {

    var currentValue: T = default

    init {
        if (file.exists()) {
            try {
                type.parse(JsonGlobals.JsonObjectMapper.readTree(file), default)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            file.parentFile.mkdirs()
        }
    }

    override fun get(transaction: Transaction): T = currentValue

    override fun set(transaction: Transaction, value: T) {
        currentValue = value
        file.writeText(JsonGlobals.jsonFactory.generateString {
            type.serialize(this, value)
        })
    }
}