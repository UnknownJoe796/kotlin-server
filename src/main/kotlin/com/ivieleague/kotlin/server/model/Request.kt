package com.ivieleague.kotlin.server.model

sealed class Request<T> {
    abstract val table: Table
    abstract fun invoke(transaction: Transaction, tableAccess: TableAccess): T

    class Update(override var table: Table, var write: Write) : Request<Instance>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): Instance = tableAccess.update(transaction, write)
    }

    class Get(override var table: Table, var id: String, var read: Read) : Request<Instance?>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): Instance? = tableAccess.get(transaction, id, read)
    }

    class Delete(override var table: Table, var id: String) : Request<Boolean>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): Boolean = tableAccess.delete(transaction, id)
    }

    class Query(override var table: Table, var condition: Condition, var read: Read) : Request<List<Instance>>() {
        override fun invoke(transaction: Transaction, tableAccess: TableAccess): List<Instance> = tableAccess.query(transaction, condition, read).toList()
    }
}