//package com.ivieleague.kotlin.server.old.auth
//
//import com.ivieleague.kotlin.server.TokenInformation
//import com.ivieleague.kotlin.server.exceptionNotFound
//import com.ivieleague.kotlin.server.old.model.*
//import com.ivieleague.kotlin.server.old.type.*
//import com.lightningkite.kotlin.castOrNull
//import org.mindrot.jbcrypt.BCrypt
//
//class UserTableAccess(val wraps: TableAccess, val tokenInformation: TokenInformation) : TableAccess {
//    val wrapsTable = wraps.table as AbstractUserTable
//    val password = Primitive(
//            key = "password",
//            description = "The password of this user",
//            type = PrimitiveType.ShortString,
//            readPermission = SecurityRules.never,
//            editPermission = { user -> user?.get("id")?.castOrNull<String>()?.let { Condition.IdEquals(id = it) } ?: Condition.Never }
//    )
//    val token = Primitive(
//            key = "token",
//            description = "A token generated for this user",
//            type = PrimitiveType.ShortString,
//            readPermission = { user -> user?.get("id")?.castOrNull<String>()?.let { Condition.IdEquals(id = it) } ?: Condition.Never },
//            editPermission = SecurityRules.never
//    )
//    override val table: Table = object : Table {
//        override val tableName: String = wraps.table.tableName
//        override val tableDescription: String = wraps.table.tableDescription
//        val primitives: Collection<Primitive> = wraps.table.primitives + password + token - wrapsTable.hash
//        override val links: Collection<Link> = wraps.table.links
//        override val multilinks: Collection<Multilink> = wraps.table.multilinks
//        override val readPermission: SecurityRule = wraps.table.readPermission
//        override val writeBeforePermission: SecurityRule = wraps.table.writeBeforePermission
//        override val writeAfterPermission: SecurityRule = wraps.table.writeAfterPermission
//    }
//
//    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
//        val isReadingToken = read.primitives.remove(token)
//        val instance = wraps.get(transaction, id, read) ?: return null
//        if (isReadingToken) instance[token.key] = tokenInformation.token(instance.id)
//        return instance
//    }
//
//    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
//        val isReadingToken = read.primitives.remove(token)
//        val result = wraps.gets(transaction, ids, read)
//        if (isReadingToken)
//            result.values.forEach { it?.let { it.scalars[token] = tokenInformation.token(it.id) } }
//        return result
//    }
//
//    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
//        val isReadingToken = read.primitives.remove(token)
//        return wraps.query(transaction, read).also {
//            if (isReadingToken) {
//                for (instance in it) {
//                    instance.scalars[token] = tokenInformation.token(instance.id)
//                }
//            }
//        }
//    }
//
//    override fun update(transaction: Transaction, write: Write): WriteResult {
//        val password = write.scalars.remove(password)
//        if (password != null) {
//            val salt = BCrypt.gensalt()
//            write.scalars[wrapsTable.hash] = BCrypt.hashpw(password.toString(), salt)
//        }
//        return wraps.update(transaction, write)
//    }
//
//    fun login(schema: Schema, usernamePrimitive: Primitive, username: String, password: String, read: Read = table.defaultRead()): Instance {
//        val isReadingToken = read.primitives.remove(token)
//        read.primitives += wrapsTable.hash
//        read.condition = Condition.ScalarEqual(primitive = usernamePrimitive, value = username)
//        val instance = Transaction(null, tableAccesses = schema).use { txn ->
//            wraps.query(txn, read = read)
//                    .firstOrNull() ?: throw exceptionNotFound("Username and password combination not found")
//        }
//        val hash = instance.scalars.remove(wrapsTable.hash).toString()
//        val passes = BCrypt.checkpw(password, hash)
//        if (!passes) throw exceptionNotFound("Username and password combination not found")
//        if (isReadingToken) instance.scalars[token] = tokenInformation.token(instance.id)
//        return instance
//    }
//}