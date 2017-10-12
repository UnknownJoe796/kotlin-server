package com.ivieleague.kotlin.server.model

import com.ivieleague.kotlin.server.type.Instance
import com.ivieleague.kotlin.server.type.Table

sealed class Request<T> {
    abstract val table: Table
    abstract fun invoke(transaction: Transaction, tableAccess: TableAccess): T

    class Update(override var table: Table, var write: Write) : Request<WriteResult>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): WriteResult = tableAccess.update(transaction, write)
    }

    class Get(override var table: Table, var id: String, var read: Read) : Request<Instance?>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): Instance? = tableAccess.get(transaction, id, read)
    }

    class Query(override var table: Table, var read: Read) : Request<List<Instance>>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): List<Instance> = tableAccess.query(transaction, read).toList()
    }
}