package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.exceptionNotFound
import com.ivieleague.kotlin.server.model.*
import org.mindrot.jbcrypt.BCrypt

class UserTableAccess(val wraps: TableAccess, val tokenInformation: TokenInformation) : TableAccess {
    val wrapsTable = wraps.table as AbstractUserTable
    val password = Scalar(
            key = "password",
            description = "The password of this user",
            type = ScalarType.ShortString,
            readPermission = SecurityRules.never,
            editPermission = { user -> user?.id?.let { Condition.IdEquals(id = it) } ?: Condition.Never }
    )
    val token = Scalar(
            key = "token",
            description = "A token generated for this user",
            type = ScalarType.ShortString,
            readPermission = { user -> user?.id?.let { Condition.IdEquals(id = it) } ?: Condition.Never },
            editPermission = SecurityRules.never
    )
    override val table: Table = object : Table {
        override val tableName: String = wraps.table.tableName
        override val tableDescription: String = wraps.table.tableDescription
        override val scalars: Collection<Scalar> = wraps.table.scalars + password + token - wrapsTable.hash
        override val links: Collection<Link> = wraps.table.links
        override val multilinks: Collection<Multilink> = wraps.table.multilinks
        override val readPermission: SecurityRule = wraps.table.readPermission
        override val writeBeforePermission: SecurityRule = wraps.table.writeBeforePermission
        override val writeAfterPermission: SecurityRule = wraps.table.writeAfterPermission
    }

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        val isReadingToken = read.scalars.remove(token)
        val instance = wraps.get(transaction, id, read) ?: return null
        if (isReadingToken) instance.scalars[token] = tokenInformation.token(instance.id)
        return instance
    }

    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
        val isReadingToken = read.scalars.remove(token)
        val result = wraps.gets(transaction, ids, read)
        if (isReadingToken)
            result.values.forEach { it?.let { it.scalars[token] = tokenInformation.token(it.id) } }
        return result
    }

    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
        val isReadingToken = read.scalars.remove(token)
        return wraps.query(transaction, read).also {
            if (isReadingToken) {
                for (instance in it) {
                    instance.scalars[token] = tokenInformation.token(instance.id)
                }
            }
        }
    }

    override fun update(transaction: Transaction, write: Write): Instance {
        val password = write.scalars.remove(password)
        if (password != null) {
            val salt = BCrypt.gensalt()
            write.scalars[wrapsTable.hash] = BCrypt.hashpw(password.toString(), salt)
        }
        return wraps.update(transaction, write)
    }

    override fun delete(transaction: Transaction, id: String): Boolean = wraps.delete(transaction, id)

    fun login(usernameScalar: Scalar, username: String, password: String, read: Read = table.defaultRead()): Instance {
        val isReadingToken = read.scalars.remove(token)
        read.scalars += wrapsTable.hash
        read.condition = Condition.ScalarEqual(scalar = usernameScalar, value = username)
        val transaction = Transaction()
        val instance = wraps.query(transaction, read = read)
                .firstOrNull() ?: throw exceptionNotFound("Username and password combination not found")
        transaction.commit()
        val hash = instance.scalars.remove(wrapsTable.hash).toString()
        val passes = BCrypt.checkpw(password, hash)
        if (!passes) throw exceptionNotFound("Username and password combination not found")
        if (isReadingToken) instance.scalars[token] = tokenInformation.token(instance.id)
        return instance
    }
}