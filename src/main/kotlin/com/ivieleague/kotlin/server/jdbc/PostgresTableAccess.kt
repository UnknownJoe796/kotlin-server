package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.Fetcher
import com.ivieleague.kotlin.server.JsonObjectMapper
import com.ivieleague.kotlin.server.model.*
import java.sql.Connection
import java.sql.ResultSet


class PostgresTableAccess(
        override val connection: Connection,
        val schema: String,
        override val table: Table,
        val tableAccesses: Fetcher<Table, TableAccess>
) : TableAccess, SQLTableAccess {
    fun jdbcToKotlin(result: ResultSet, scalar: Scalar, prepend: String = ""): Any? {
        val type = scalar.type
        val key = prepend + scalar.key
        return when (type) {
            ScalarType.Boolean -> result.getBoolean(key)
            ScalarType.Byte -> result.getByte(key)
            ScalarType.Short -> result.getShort(key)
            ScalarType.Int -> result.getInt(key)
            ScalarType.Long -> result.getLong(key)
            ScalarType.Float -> result.getFloat(key)
            ScalarType.Double -> result.getDouble(key)
            ScalarType.ShortString -> result.getString(key)
            ScalarType.LongString -> result.getString(key)
            ScalarType.JSON -> JsonObjectMapper.readValue(result.getString(key), Any::class.java)
            ScalarType.Date -> result.getDate(key)
            is ScalarType.Enum -> type.enum[result.getByte(key)]
        }
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


    //    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
//        val result = connection.createStatement().executeQuery("SELECT id, ${properties.joinToString { it.name }} FROM ${table.tableName} WHERE id = ${id.toLong()}")
//        if (!result.next()) return null
//        return Instance(table, id, properties.associate {
//            it to jdbcToKotlin(it, result)
//        })
//    }
//
//    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
//        if (id != null) {
//            connection.createStatement().executeUpdate("UPDATE ${table.tableName} SET ${inProperties.entries.joinToString {
//                val type = it.key.type
//                it.key.name + " = " + mapFromKotlin(it.key, it.value)
//            }} WHERE id = ${id.toLong()};")
//            return get(table, id, outProperties)!!
//        } else {
//            val properties = inProperties.toList()
//            val statement = connection.createStatement()
//            statement.executeUpdate("INSERT INTO ${table.tableName} (${properties.joinToString { it.first.name }}) VALUES (${properties.joinToString {
//                mapFromKotlin(it.first, it.second)
//            }});")
//            val generatedKeys = statement.generatedKeys
//            generatedKeys.next()
//            return get(table, generatedKeys.getInt("id").toString(), outProperties)!!
//        }
//    }

}