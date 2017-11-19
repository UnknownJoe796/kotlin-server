package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SType
import java.util.*

class MemoryDAO<T>(override val type: SType<T>) : DAO<T> {
    val data = HashMap<String, T>()

    override fun get(transaction: Transaction, pointer: String): T? = data[pointer]

    override fun create(transaction: Transaction, value: T): String {
        val newId = UUID.randomUUID().toString()
        data[newId] = value
        return newId
    }

    override fun set(transaction: Transaction, pointer: String, value: T) {
        data[pointer] = value
    }

    override fun delete(transaction: Transaction, pointer: String) {
        data.remove(pointer)
    }
}