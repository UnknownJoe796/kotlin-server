package com.ivieleague.kotlin.server.core


interface TableAccess {
    val table: Table
    fun get(user: Instance?, id: String, read: Read): Instance?
    fun query(user: Instance?, condition: Condition, read: Read): Collection<Instance>
    fun update(user: Instance?, write: Write): Instance
    fun delete(user: Instance?, id: String): Boolean
}