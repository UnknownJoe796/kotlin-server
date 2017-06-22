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

    fun Instance.secureProperties(user: Instance?): Instance {
        return Instance(
                id = id,
                scalars = scalars.filter { it.key.readPermission.invoke(user).invoke(this) },
                links = links.filter { it.key.readPermission.invoke(user).invoke(this) },
                multilinks = multilinks.filter { it.key.readPermission.invoke(user).invoke(this) }
        )
    }

    fun Instance.securePropertiesPreWrite(user: Instance?) {
        for ((property, _) in scalars) {
            if (!property.writeBeforePermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }
        for ((property, _) in links) {
            if (!property.writeBeforePermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }
        for ((property, _) in multilinks) {
            if (!property.writeBeforePermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }
    }

    fun Write.secureProperties(user: Instance?): Write {
        for ((property, _) in scalars) {
            if (!property.writeAfterPermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }
        for ((property, _) in links) {
            if (!property.writeAfterPermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }
        for ((property, _) in multilinks) {
            if (!property.writeAfterPermission.invoke(user).invoke(this)) {
                throw IllegalArgumentException("You can't write this value to this property.")
            }
        }

        return this
    }

    fun Write.prewriteQuery(user: Instance?) = Read().also {
        table.writeBeforePermission.invoke(user).dependencies(it)
        for ((property, _) in scalars) {
            property.writeAfterPermission.invoke(user).dependencies(it)
        }
        for ((property, _) in links) {
            property.writeAfterPermission.invoke(user).dependencies(it)
        }
        for ((property, _) in multilinks) {
            property.writeAfterPermission.invoke(user).dependencies(it)
        }
    }

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        val readCondition = table.readPermission(user)
        readCondition.dependencies(read)
        val result = wraps.get(user, id, read) ?: return null
        if (readCondition.invoke(result))
            return result.secureProperties(user)
        else
            throw IllegalAccessException("You can't read this because you don't have permission to access this row")
    }

    override fun query(user: Instance?, condition: Condition, read: Read): Collection<Instance> {
        val readCondition = table.readPermission(user)
        readCondition.dependencies(read)
        return wraps.query(
                user = user,
                condition = Condition.AllCondition(listOf(condition, readCondition)),
                read = read
        ).map { it.secureProperties(user) }
    }

    override fun update(user: Instance?, write: Write): Instance {
        val writeBeforeCondition = table.writeBeforePermission(user)
        val writeAfterCondition = table.writeAfterPermission(user)
        if (writeAfterCondition != Condition.Always) {
            if (!writeAfterCondition.invoke(write))
                throw IllegalAccessException("You can't write this because the data you are attempting to write is restricted")
        }
        val read = write.prewriteQuery(user)
        if (write.id != null) {
            val existing = if (!read.isEmpty()) {
                get(user, write.id!!, read)
            } else Instance(write.id!!)

            if (existing != null) {
                if (!writeBeforeCondition.invoke(existing))
                    throw IllegalAccessException("You can't write this because you don't have permission to modify this row")
                existing.securePropertiesPreWrite(user)
            }
        }
        write.secureProperties(user)
        return wraps.update(user, write)
    }

    override fun delete(user: Instance?, id: String): Boolean {
        val writeBeforeCondition = table.writeBeforePermission(user)
        if (writeBeforeCondition != Condition.Always) {
            val read = Read().apply { writeBeforeCondition.dependencies(this) }
            val existing = get(user, id, read)
            if (existing != null)
                if (!writeBeforeCondition.invoke(existing))
                    throw IllegalAccessException("You can't delete this because you don't have permission to modify this row")
        }
        return wraps.delete(user, id)
    }
}