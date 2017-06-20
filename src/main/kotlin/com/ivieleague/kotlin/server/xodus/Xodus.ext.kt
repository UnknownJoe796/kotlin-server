package com.ivieleague.kotlin.server.xodus

import jetbrains.exodus.entitystore.StoreTransaction

inline fun <T> StoreTransaction.use(action: (StoreTransaction) -> T): T {
    return try {
        val result = action.invoke(this)
        commit()
        result
    } catch(e: Exception) {
        abort()
        throw e
    }
}