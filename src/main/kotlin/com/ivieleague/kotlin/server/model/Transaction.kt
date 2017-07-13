package com.ivieleague.kotlin.server.model

import com.lightningkite.kotlin.runAll

/**
 * Represents a database transaction
 * Created by josep on 7/12/2017.
 */
class Transaction(
        val user: Instance? = null,
        val readOnly: Boolean = false
) {
    val onCommit = ArrayList<() -> Unit>()
    val onFail = ArrayList<() -> Unit>()
    var finished = false

    fun commit() {
        if (finished) return
        finished = true
        onCommit.runAll()
    }

    fun fail() {
        if (finished) return
        finished = true
        onFail.runAll()
    }
}

inline fun Transaction.use(action: (Transaction) -> Unit) = try {
    action.invoke(this)
    commit()
} finally {
    fail()
}