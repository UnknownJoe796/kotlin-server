package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.model.Transaction
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.StoreTransaction
import java.util.*

inline fun <T> StoreTransaction.use(action: (StoreTransaction) -> T): T {
    return try {
        val result = action.invoke(this)
        commit()
        result
    } catch (e: Exception) {
        abort()
        throw e
    }
}

fun StoreTransaction.getEntity(id: String) = getEntity(toEntityId(id))
fun StoreTransaction.getEntityOrNull(id: String) = try {
    getEntity(toEntityId(id))
} catch (e: EntityRemovedInDatabaseException) {
    null
}

val Transaction_xodus = WeakHashMap<PersistentEntityStore, WeakHashMap<Transaction, StoreTransaction>>()
fun Transaction.getXodus(store: PersistentEntityStore): StoreTransaction {
    return Transaction_xodus.getOrPut(store) { WeakHashMap() }.getOrPut(this) {
        val txn = if (this.readOnly) store.beginReadonlyTransaction()
        else store.beginTransaction()

        this.onCommit += { txn.commit() }
        this.onFail += { txn.abort() }

        txn
    }
}