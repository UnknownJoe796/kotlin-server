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

    override fun get(user: Instance?, id: String, read: Read): Instance? {
        val isReadingToken = read.scalars.remove(token)
        val instance = wraps.get(user, id, read) ?: return null
        if (isReadingToken) instance.scalars[token] = tokenInformation.token(instance.id)
        return instance
    }

    override fun query(user: Instance?, condition: Condition, read: Read): Collection<Instance> {
        val isReadingToken = read.scalars.remove(token)
        return wraps.query(user, condition, read).also {
            if (isReadingToken) {
                for (instance in it) {
                    instance.scalars[token] = tokenInformation.token(instance.id)
                }
            }
        }
    }

    override fun update(user: Instance?, write: Write): Instance {
        val password = write.scalars.remove(password)
        if (password != null) {
            val salt = BCrypt.gensalt()
            write.scalars[wrapsTable.hash] = BCrypt.hashpw(password.toString(), salt)
        }
        return wraps.update(user, write)
    }

    override fun delete(user: Instance?, id: String): Boolean = wraps.delete(user, id)

    fun login(usernameScalar: Scalar, username: String, password: String, read: Read = table.defaultRead()): Instance {
        val isReadingToken = read.scalars.remove(token)
        read.scalars += wrapsTable.hash
        val instance = wraps.query(null, condition = Condition.ScalarEqual(scalar = usernameScalar, value = username), read = read)
                .firstOrNull() ?: throw exceptionNotFound("Username and password combination not found")
        val hash = instance.scalars.remove(wrapsTable.hash).toString()
        val passes = BCrypt.checkpw(password, hash)
        if (!passes) throw exceptionNotFound("Username and password combination not found")
        if (isReadingToken) instance.scalars[token] = tokenInformation.token(instance.id)
        return instance
    }
}