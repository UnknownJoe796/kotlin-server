package com.ivieleague.kotlin.server.model


interface TableAccess {
    val table: Table
    fun get(transaction: Transaction, id: String, read: Read): Instance?
    fun query(transaction: Transaction, condition: Condition, read: Read): Collection<Instance>
    fun update(transaction: Transaction, write: Write): Instance
    fun delete(transaction: Transaction, id: String): Boolean
}