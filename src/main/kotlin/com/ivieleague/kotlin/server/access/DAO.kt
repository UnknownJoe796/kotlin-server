package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SType

/**
 * A DAO (Data Access Object) which allows the user to get, create, set, and delete objects in a database.
 */
interface DAO<T> {
    val type: SType<T>
    fun get(transaction: Transaction, pointer: String): T?
    fun create(transaction: Transaction, value: T): String
    fun set(transaction: Transaction, pointer: String, value: T)
    fun delete(transaction: Transaction, pointer: String)
}

