package com.ivieleague.kotlin.server.sql

import com.ivieleague.kotlin.server.model.Transaction
import java.sql.Connection
import java.util.*

val Transaction_jdbc = WeakHashMap<Connection, WeakHashMap<Transaction, JDBCTransaction>>()
fun Transaction.getJdbc(connection: Connection): JDBCTransaction {
    return Transaction_jdbc.getOrPut(connection) { WeakHashMap() }.getOrPut(this) {
        val txn = JDBCTransaction(connection)
        onCommit += { txn.commit() }
        onFail += { txn.fail() }
        txn
    }
}

class JDBCTransaction(val connection: Connection) {
    init {
        connection.autoCommit = false
    }

    fun fail() {
        connection.rollback()
    }

    fun commit() {
        connection.commit()
        connection.autoCommit = true
    }
}