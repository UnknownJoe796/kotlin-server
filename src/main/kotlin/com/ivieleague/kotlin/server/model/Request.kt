package com.ivieleague.kotlin.server.model

sealed class Request<T> {
    abstract val table: Table
    abstract fun invoke(user: Instance?, tableAccess: TableAccess): T

    class Update(override var table: Table, var write: Write) : Request<Instance>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Instance = tableAccess.update(user, write)
    }

    class Get(override var table: Table, var id: String, var read: Read) : Request<Instance?>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Instance? = tableAccess.get(user, id, read)
    }

    class Delete(override var table: Table, var id: String) : Request<Boolean>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): Boolean = tableAccess.delete(user, id)
    }

    class Query(override var table: Table, var condition: Condition, var read: Read) : Request<List<Instance>>() {
        override fun invoke(user: Instance?, tableAccess: TableAccess): List<Instance> = tableAccess.query(user, condition, read).toList()
    }
}