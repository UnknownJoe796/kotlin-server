package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SType

/**
 * A DAO (Data Access Object) which allows the user to get, create, set, and delete objects in a database.
 */
interface SingletonAccess<T> {
    val type: SType<T>
    fun get(transaction: Transaction): T
    fun set(transaction: Transaction, value: T)
}

/**
 * A DAO (Data Access Object) which allows the user to get, create, set, and delete objects in a database.
 */
interface SingletonModifyAccess<T, M> : SingletonAccess<T> {
    val modifyType: SType<M>
    fun modify(transaction: Transaction, value: M)
}