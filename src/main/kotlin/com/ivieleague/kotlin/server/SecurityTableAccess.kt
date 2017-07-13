package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.*
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

    fun Instance.secureProperties(transaction: Transaction) {
        scalars.keys.removeAll { !it.readPermission.invoke(transaction.user).invoke(this) }
        links.keys.removeAll { !it.readPermission.invoke(transaction.user).invoke(this) }
        multilinks.keys.removeAll { !it.readPermission.invoke(transaction.user).invoke(this) }
    }

    fun Instance.securePropertiesPreWrite(transaction: Transaction) {
        for ((property, _) in scalars) {
            if (!property.editPermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }
        for ((property, _) in links) {
            if (!property.editPermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }
        for ((property, _) in multilinks) {
            if (!property.editPermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }
    }

    fun Write.secureProperties(transaction: Transaction): Write {
        for ((property, _) in scalars) {
            if (!property.writePermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }
        for ((property, _) in links) {
            if (!property.writePermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }
        for ((property, _) in multilinks) {
            if (!property.writePermission.invoke(transaction.user).invoke(this)) {
                throw exceptionForbidden("You can't write this value to this property.")
            }
        }

        return this
    }

    fun Write.prewriteQuery(transaction: Transaction) = Read().also {
        table.writeBeforePermission.invoke(transaction.user).dependencies(it)
        for ((property, _) in scalars) {
            property.writePermission.invoke(transaction.user).dependencies(it)
        }
        for ((property, _) in links) {
            property.writePermission.invoke(transaction.user).dependencies(it)
        }
        for ((property, _) in multilinks) {
            property.writePermission.invoke(transaction.user).dependencies(it)
        }
    }

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        val readCondition = table.readPermission(transaction.user)
        readCondition.dependencies(read)
        val result = wraps.get(transaction, id, read) ?: return null
        if (readCondition.invoke(result))
            return result.apply { secureProperties(transaction) }
        else
            throw exceptionForbidden("You can't read this because you don't have permission to access this row")
    }

    override fun query(transaction: Transaction, condition: Condition, read: Read): Collection<Instance> {
        val readCondition = table.readPermission(transaction.user)
        readCondition.dependencies(read)
        return wraps.query(
                transaction = transaction,
                condition = Condition.AllConditions(listOf(condition, readCondition)),
                read = read
        ).also {
            for (item in it) {
                item.secureProperties(transaction)
            }
        }
    }

    override fun update(transaction: Transaction, write: Write): Instance {
        val writeBeforeCondition = table.writeBeforePermission(transaction.user)
        val writeAfterCondition = table.writeAfterPermission(transaction.user)
        if (writeAfterCondition != Condition.Always) {
            if (!writeAfterCondition.invoke(write))
                throw exceptionForbidden("You can't write this because the data you are attempting to write is restricted")
        }
        val read = write.prewriteQuery(transaction)
        if (write.id != null) {
            val existing = if (!read.isEmpty()) {
                get(transaction, write.id!!, read)
            } else Instance(table, write.id!!)

            if (existing != null) {
                if (!writeBeforeCondition.invoke(existing))
                    throw exceptionForbidden("You can't write this because you don't have permission to modify this row")
                existing.securePropertiesPreWrite(transaction)
            }
        }
        write.secureProperties(transaction)
        return wraps.update(transaction, write)
    }

    override fun delete(transaction: Transaction, id: String): Boolean {
        val writeBeforeCondition = table.writeBeforePermission(transaction.user)
        if (writeBeforeCondition != Condition.Always) {
            val read = Read().apply { writeBeforeCondition.dependencies(this) }
            val existing = get(transaction, id, read)
            if (existing != null)
                if (!writeBeforeCondition.invoke(existing))
                    throw exceptionForbidden("You can't delete this because you don't have permission to modify this row")
        }
        return wraps.delete(transaction, id)
    }
}