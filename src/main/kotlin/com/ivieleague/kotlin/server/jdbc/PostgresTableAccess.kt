package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.Fetcher
import com.ivieleague.kotlin.server.JsonObjectMapper
import com.ivieleague.kotlin.server.model.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class PostgresTableAccess(
        val connection: Connection,
        val schema: String,
        override val table: Table,
        val tableAccesses: Fetcher<Table, TableAccess>
) : TableAccess {

    val sqlTable = table.toSql()
    val sqlRelations = table.toMultilinkTablesSql()

    init {
        connection.createStatement().execute(sqlTable.toDefineIfNotExistsString())
        for ((_, relationSql) in sqlRelations) {
            connection.createStatement().execute(relationSql.toDefineIfNotExistsString())
        }
    }

    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
        return super.get(transaction, id, read)
    }

    override fun gets(transaction: Transaction, ids: Collection<String>, read: Read): Map<String, Instance?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(transaction: Transaction, write: Write): Instance {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(transaction: Transaction, id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //convert table to sql

    //convert read to sql - only one layer deep

    //convert write to sql - only one layer deep


    //convert ResultSet to instances
    fun ResultSet.readScalar(scalar: Scalar): Any? {
        val type = scalar.type
        val key = scalar.key
        return when (type) {
            ScalarType.Boolean -> getBoolean(key)
            ScalarType.Byte -> getByte(key)
            ScalarType.Short -> getShort(key)
            ScalarType.Int -> getInt(key)
            ScalarType.Long -> getLong(key)
            ScalarType.Float -> getFloat(key)
            ScalarType.Double -> getDouble(key)
            ScalarType.ShortString -> getString(key)
            ScalarType.LongString -> getString(key)
            ScalarType.JSON -> JsonObjectMapper.readValue(getString(key), Any::class.java)
            ScalarType.Date -> getDate(key)
            is ScalarType.Enum -> type.enum[getByte(key)]
        }
    }

    fun ResultSet.toInstances(transaction: Transaction, read: Read): Map<String, Instance?> {
        val instances = HashMap<String, Instance?>()
        val links = HashMap<Link, HashMap<Instance, String>>()
        while (next()) {
            try {
                val id = this.getLong("_id").toString()
                val result = Instance(table, id)
                for (scalar in read.scalars) {
                    result.scalars[scalar] = try {
                        this.readScalar(scalar)
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                }
                for ((link, _) in read.links) {
                    val linkId = try {
                        getString(link.key)
                    } catch (e: SQLException) {
                        e.printStackTrace()
                        null
                    }
                    if (linkId != null) {
                        links.getOrPut(link) { HashMap() }[result] = linkId
                    }
                }
                instances[id] = result

            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        for ((link, subread) in read.links) {
            TableAccessCommon.deferPopulateLinks(transaction, link, transaction.tableAccesses[link.table], subread, links[link]!!)
        }

        for ((multilink, subread) in read.multilinks) {
            val multilinkTableName = table.tableName + "_" + multilink.key
            //TODO: Transactions
            val resultSet = connection.createStatement().executeQuery("SELECT owner, owned FROM $multilinkTableName WHERE owner IN (${instances.keys.joinToString(",")})")
            val idsMap = HashMap<Instance, ArrayList<String>>()
            while (resultSet.next()) {
                val owner = resultSet.getLong("owner").toString()
                val owned = resultSet.getLong("owned").toString()
                val instance = instances[owner]
                if (instance != null) {
                    idsMap.getOrPut(instance) { ArrayList() }.add(owned)
                }
            }
            TableAccessCommon.deferPopulateMultilinks(transaction, multilink, transaction.tableAccesses[multilink.table], subread, idsMap)
        }

        return instances
    }
}