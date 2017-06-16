package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.core.*
import java.util.*

class SecurityDatabaseAccess(val wraps: Fetcher<Table, TableAccess>) : Fetcher<Table, TableAccess> {
    private val cached = HashMap<Table, SecurityTableAccess>()
    override fun get(key: Table): TableAccess = cached.getOrPut(key) {
        SecurityTableAccess(wraps[key])
    }
}

class SecurityTableAccess(val wraps: TableAccess) : TableAccess {

    override val table: Table
        get() = wraps.table

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        val readCondition = table.readPermission(user)
        readCondition.dependencies(read)
        val result = wraps.get(user, id, read) ?: return null
        if (readCondition.invoke(result))
            return result
        else
            throw IllegalAccessException("You can't read this because you don't have permission to access this row")
    }

    override fun query(user: Instance?, condition: Condition, read: Read): List<Instance> {
        val readCondition = table.readPermission(user)
        readCondition.dependencies(read)
        return wraps.query(
                user = user,
                condition = Condition.AllCondition(listOf(condition, readCondition)),
                read = read
        )
    }

    override fun update(user: Instance?, write: Write): Instance {
        val writeCondition = table.writePermission(user)
        if (writeCondition != Condition.Always) {
            if (!writeCondition.invoke(write))
                throw IllegalAccessException("You can't write this because the data you are attempting to write is restricted")
            val read = Read().apply { writeCondition.dependencies(this) }
            if (write.id != null) {
                val existing = get(user, write.id!!, read)
                if (existing != null)
                    if (!writeCondition.invoke(existing))
                        throw IllegalAccessException("You can't write this because you don't have permission to modify this row")
            }
        }
        return wraps.update(user, write)
    }

    override fun delete(user: Instance?, id: String): Boolean {
        val writeCondition = table.writePermission(user)
        if (writeCondition != Condition.Always) {
            val read = Read().apply { writeCondition.dependencies(this) }
            val existing = get(user, id, read)
            if (existing != null)
                if (!writeCondition.invoke(existing))
                    throw IllegalAccessException("You can't delete this because you don't have permission to modify this row")
        }
        return wraps.delete(user, id)
    }
}