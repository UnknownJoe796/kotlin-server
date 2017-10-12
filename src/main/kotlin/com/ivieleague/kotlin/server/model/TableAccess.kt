package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Table


interface TableAccess {
    val table: Table
    fun get(transaction: Transaction, id: String, read: Read): Instance? = gets(transaction, listOf(id), read)[id]
    fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?>
    fun query(transaction: Transaction, read: Read): Collection<Instance>
    fun update(transaction: Transaction, write: Write): WriteResult
}