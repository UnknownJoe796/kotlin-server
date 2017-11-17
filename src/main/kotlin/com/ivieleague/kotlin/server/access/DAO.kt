package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SimpleTypedObject

interface DAO {
    val type: SClass
    fun query(transaction: Transaction, read: SimpleTypedObject): List<SimpleTypedObject>
    fun update(transaction: Transaction, write: SimpleTypedObject): SimpleTypedObject
}