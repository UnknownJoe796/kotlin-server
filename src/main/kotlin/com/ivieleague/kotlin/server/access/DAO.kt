package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.TypedObject

interface DAO {
    val type: SClass
    fun get(transaction: Transaction, id: String, read: TypedObject): TypedObject?
    fun query(transaction: Transaction, read: TypedObject): List<TypedObject>
    fun update(transaction: Transaction, write: TypedObject): TypedObject
}