package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.*

sealed class Request<T> {
    abstract val table: Table
    abstract fun invoke(user: Instance?, tableAccess: TableAccess): T

    class Update(override val table: Table, val write: Write) : Request<Instance>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Instance = tableAccess.update(user, write)
    }

    class Get(override val table: Table, val id: String, val read: Read) : Request<Instance?>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Instance? = tableAccess.get(user, id, read)
    }

    class Delete(override val table: Table, val id: String) : Request<Boolean>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Boolean = tableAccess.delete(user, id)
    }

    class Query(override val table: Table, val condition: Condition, val read: Read) : Request<List<Instance>>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): List<Instance> = tableAccess.query(user, condition, read).toList()
    }
}