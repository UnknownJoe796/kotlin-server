package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.TypedObject

/**
 * A DAO that can be queried.
 */
interface QueryableDAO<T> : DAO<T> {
    fun query(
            transaction: Transaction,
            condition: TypedObject,
            sort: List<Sort>,
            count: Int,
            start: T?
    ): List<T>
}