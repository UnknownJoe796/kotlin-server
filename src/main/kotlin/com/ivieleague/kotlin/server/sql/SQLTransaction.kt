package com.ivieleague.kotlin.server.sql

import com.ivieleague.kotlin.server.model.Transaction
import java.sql.Connection
import java.util.*

val Transaction_xodus = WeakHashMap<C, WeakHashMap<Transaction, StoreTransaction>>()
fun Transaction.getXodus(store: PersistentEntityStore): StoreTransaction {
    return Transaction_xodus.getOrPut(store) { WeakHashMap() }.getOrPut(this) {
        val txn = if (this.readOnly) store.beginReadonlyTransaction()
        else store.beginTransaction()

        this.onCommit += { txn.commit() }
        this.onFail += { txn.abort() }

        txn
    }
    class SQLTransaction(val connection: Connection) {
        init {
            connection.autoCommit = false

        }
    }