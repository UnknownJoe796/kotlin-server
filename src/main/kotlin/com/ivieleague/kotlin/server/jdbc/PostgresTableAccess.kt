//package com.ivieleague.kotlin.server.jdbc
//
//import com.ivieleague.kotlin.server.JsonObjectMapper
//import com.ivieleague.kotlin.server.model.*
//import com.ivieleague.kotlin.server.sql.*
//import java.sql.Connection
//import java.sql.ResultSet
//import java.sql.SQLException
//
//class PostgresTableAccess(
//        val connection: Connection,
//        val schema: String,
//        override val table: Table
//) : TableAccess {
//
//    val sqlScalars = table.primitives.associate { it to it.toSql() }
//    val sqlLinks = table.links.associate { it to it.toSql() }
//    val sqlMultilinks = table.multilinks.associate { it to it.toSql(table.tableName) }
//    val sqlTable = SQLTable(
//            name = table.tableName,
//            description = table.tableDescription,
//            columns = listOf(StandardPrimaryKey) + sqlScalars.values + sqlLinks.values,
//            primaryKey = listOf(StandardPrimaryKey)
//    )
//
//    init {
//        connection.createIfNotExists(sqlTable)
//        for ((_, relationSql) in sqlMultilinks) {
//            connection.createIfNotExists(relationSql)
//        }
//    }
//
//    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
//        return super.get(transaction, id, read)
//    }
//
//    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun update(transaction: Transaction, write: Write): WriteResult {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    //convert read to sql - only one layer deep
//
//    //convert write to sql - only one layer deep
//    fun Write.toSql(pastLayers: Map<Link, Instance?>): SQLUpsert {
//        return SQLUpsert(
//                sqlTable,
//                (this.id?.toLong()?.let { mapOf(StandardPrimaryKey to SQLLiteral.LInteger(it)) } ?: mapOf()) +
//                        this.primitives.entries.associate { sqlScalars[it.key]!! to it.value.toSQLLiteral() } +
//                        pastLayers.entries.associate { sqlLinks[it.key]!! to it.value?.id.toSQLLiteral() }
//        )
//    }
//
//    fun Write.handleMultilink(id: Long, transaction: Transaction) {
//        this.multilinks.forEach { (multilink, modifications) ->
//            val sqlTable = sqlMultilinks[multilink]!!
//
//            //Handle replacements
//            val replacements = modifications.replacements
//            if (replacements != null) {
//                SQLDelete(
//                        table = sqlTable,
//                        where = SQLCondition.Equal(SQLDirectColumn(StandardOwnerKey), SQLLiteral.LInteger(id))
//                )
//            }
//
//            //Handle additions
//
//            //Handle removals
//
//        }
//        val table = sqlMultilinks[multilink]!!
//        val inserts = instances.flatMap { instance ->
//            instance.multilinks.map {
//                val otherResult = transaction.tableAccesses[it.key.table].update()
//                SQLInsert(
//                        table = table,
//                        values = mapOf(
//                                StandardOwnerKey to SQLLiteral.LInteger(instance.id.toLong()),
//                                StandardOwnedKey to SQLLiteral.LString(it)
//                        )
//                )
//            }
//        }
//    }
//
//
//    //convert ResultSet to instances
//    fun ResultSet.readScalar(primitive: Primitive): Any? {
//        val type = primitive.type
//        val key = sqlScalars[primitive]!!.toString()
//        return when (type) {
//            PrimitiveType.Boolean -> getBoolean(key)
//            PrimitiveType.Byte -> getByte(key)
//            PrimitiveType.Short -> getShort(key)
//            PrimitiveType.Int -> getInt(key)
//            PrimitiveType.Long -> getLong(key)
//            PrimitiveType.Float -> getFloat(key)
//            PrimitiveType.Double -> getDouble(key)
//            PrimitiveType.ShortString -> getString(key)
//            PrimitiveType.LongString -> getString(key)
//            PrimitiveType.JSON -> JsonObjectMapper.readValue(getString(key), Any::class.java)
//            PrimitiveType.Date -> getDate(key)
//            is PrimitiveType.Enum -> type.enum[getByte(key)]
//        }
//    }
//
//    fun ResultSet.toInstances(transaction: Transaction, read: Read): Map<String, Instance?> {
//        val instances = HashMap<String, Instance?>()
//        val links = HashMap<Link, HashMap<Instance, String>>()
//        while (next()) {
//            try {
//                val id = this.getLong("_id").toString()
//                val result = Instance(table, id)
//                for (primitive in read.primitives) {
//                    result.primitives[primitive] = try {
//                        this.readScalar(primitive)
//                    } catch (e: SQLException) {
//                        e.printStackTrace()
//                    }
//                }
//                for ((link, _) in read.links) {
//                    val linkId = try {
//                        getString(sqlLinks[link]!!.toString())
//                    } catch (e: SQLException) {
//                        e.printStackTrace()
//                        null
//                    }
//                    if (linkId != null) {
//                        links.getOrPut(link) { HashMap() }[result] = linkId
//                    }
//                }
//                instances[id] = result
//
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//
//        for ((link, subread) in read.links) {
//            TableAccessCommon.deferPopulateLinks(transaction, link, transaction.tableAccesses[link.table], subread, links[link]!!)
//        }
//
//        for ((multilink, subread) in read.multilinks) {
//            //TODO: Transactions
//
//            val source = SQLDataSourceAccess(sqlMultilinks[multilink]!!, "multilink")
//            val ownerKey = SQLResultColumn(source, StandardOwnerKey)
//            val ownedKey = SQLResultColumn(source, StandardOwnedKey)
//            val query = SQLQuery(
//                    listOf(ownerKey, ownedKey),
//                    listOf(source),
//                    listOf(),
//                    SQLCondition.In(ownerKey, SQLLiteral.LList(instances.keys.mapNotNull { it.toLongOrNull()?.let { SQLLiteral.LInteger(it) } })),
//                    listOf()
//            )
//
//            val resultSet = connection.query(query)
//            val idsMap = HashMap<Instance, ArrayList<String>>()
//            while (resultSet.next()) {
//                val owner = resultSet.getLong(ownerKey.toString()).toString()
//                val owned = resultSet.getString(ownedKey.toString())
//                val instance = instances[owner]
//                if (instance != null) {
//                    idsMap.getOrPut(instance) { ArrayList() }.add(owned)
//                }
//            }
//            TableAccessCommon.deferPopulateMultilinks(transaction, multilink, transaction.tableAccesses[multilink.table], subread, idsMap)
//        }
//
//        return instances
//    }
//}