package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.TypedObject

interface DAO {
    val type: SClass
    fun query(transaction: Transaction, query: Query, read: TypedObject): List<TypedObject>
    fun update(transaction: Transaction, write: TypedObject): TypedObject
}