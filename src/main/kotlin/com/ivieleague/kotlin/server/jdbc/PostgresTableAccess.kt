package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.JsonObjectMapper
import com.ivieleague.kotlin.server.model.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*


class PostgresTableAccess(
        val connection: Connection,
        val schema: String,
        override val table: Table,
        val tableAccesses: Fetcher<Table, TableAccess>
) : TableAccess, PostgresTableAccessWrapper {

    //region TABLE CREATION

    fun ScalarType.mapped(): String = when (this) {
        ScalarType.Boolean -> "BOOL"
        ScalarType.Byte -> "SMALLINT"
        ScalarType.Short -> "SMALLINT"
        ScalarType.Int -> "INT"
        ScalarType.Long -> "BIGINT"
        ScalarType.Float -> "REAL"
        ScalarType.Double -> "DOUBLE PRECISION"
        ScalarType.ShortString -> "VARCHAR(255)"
        ScalarType.LongString -> "TEXT"
        ScalarType.JSON -> "JSON"
        ScalarType.Date -> "TIMESTAMP"
        is ScalarType.Enum -> "SMALLINT"
    }

    fun createTable(table: Table) {
        val createMyTable = """CREATE TABLE ${table.tableName}(
                    id INT  SERIAL PRIMARY KEY,""" +
                table.scalars.joinToString(",\n") {
                    it.run { "$key ${type.mapped()}" }
                } + "," +
                table.links.joinToString(",\n") {
                    it.run {
                        if (tableAccesses[table] is PostgresTableAccessWrapper) {
                            "$key INT"
                        } else {
                            "$key VARCHAR(255)"
                        }
                    }
                } + """,
                    PRIMARY KEY (id)
                );"""

        connection.createStatement().execute(createMyTable)
    }

    fun createMultilinkTables() {
        val createMultilinkTables = table.multilinks.map { multilink ->
            """CREATE TABLE ${table.tableName + "_" + multilink.table.tableName}(
                    owner INT,
                    owned ${if (tableAccesses[multilink.table] is PostgresTableAccessWrapper) "INT" else "VARCHAR(255)"},
                    PRIMARY KEY (owner, owned),
                    FOREIGN KEY (owner) REFERENCES ${table.tableName},
                    FOREIGN KEY (owned) REFERENCES ${multilink.table.tableName}
            );"""
        }
        connection.createStatement().execute(createMultilinkTables.joinToString("\n"))
    }

    //endregion

    //region INSTANCE CONVERSION

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

    fun jdbcToInstance(resultSet: ResultSet, prepend: String = ""): List<Instance> = TODO()

    fun kotlinToSQL(property: Scalar, value: Any?): String {
        val type = property.type
        return when (type) {
            ScalarType.Boolean -> (value as Boolean).toString()
            ScalarType.Byte -> (value as Byte).toString()
            ScalarType.Short -> (value as Short).toString()
            ScalarType.Int -> (value as Int).toString()
            ScalarType.Long -> (value as Long).toString()
            ScalarType.Float -> (value as Float).toString()
            ScalarType.Double -> (value as Double).toString()
            ScalarType.ShortString -> "'$value'"
            ScalarType.LongString -> "'$value'"
            ScalarType.JSON -> JsonObjectMapper.writeValueAsString(value)
            ScalarType.Date -> Timestamp((value as Date).time).toString()
            is ScalarType.Enum -> (value as ServerEnum.Value).value.toString()
        }
    }

    fun kotlinToSQL(property: Link, value: Instance): String = value.id

    fun writeToSQL(write: Write): String = TODO()

    //endregion


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