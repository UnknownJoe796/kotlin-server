package com.ivieleague.kotlin.server.xodus

import com.ivieleague.kotlin.server.access.Transaction
import jetbrains.exodus.entitystore.Entity
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

fun <T> Entity.setPropertyNullable(key: String, value: Comparable<T>?)
        = if (value == null) deleteProperty(key) else setProperty(key, value)

//fun StoreTransaction.getTypedObject(type:SClass, id: String) = getEntityOrNull(id)?.let{
//    val result = SimpleTypedObject(type)
//    for(field in type.fields.values){
//        when(field.type){
//            is SClass -> result[field.key] = it.getLink()
//            else -> result[field.key] = it.getProperty(field.key)
//        }
//    }
//}