package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SType

/**
 * A DAO that supports modify operations.
 */
interface ModifyDAO<T, M> : DAO<T> {
    val modifyType: SType<M>
    fun modify(transaction: Transaction, pointer: String, value: M)
}

