package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.Fetcher
import com.lightningkite.kotlin.invokeAll

/**
 * Represents a database transaction
 * Created by josep on 7/12/2017.
 */
class Transaction(
        val user: Instance? = null,
        val tableAccesses: Fetcher<Table, TableAccess>,
        val readOnly: Boolean = false
) {
    val onCommit = ArrayList<() -> Unit>()
    val onFail = ArrayList<() -> Unit>()
    var finished = false

    fun commit() {
        if (finished) return
        finished = true
        onCommit.invokeAll()
    }

    fun fail() {
        if (finished) return
        finished = true
        onFail.invokeAll()
    }
}

inline fun Transaction.use(action: (Transaction) -> Unit) = try {
    action.invoke(this)
    commit()
} finally {
    fail()
}