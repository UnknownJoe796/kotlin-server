//package com.ivieleague.kotlin.server.jdbc
//
//import com.ivieleague.kotlin.server.model.*
//import java.sql.Connection
//import java.sql.ResultSet
//import java.sql.Timestamp
//import java.util.*
//
//
//class JDBCTableAccess(
//        val connection: Connection,
//        val database: String,
//        override val table:Table,
//        val tableAccesses: Fetcher<Table, TableAccess>
//) : TableAccess {
//
//    fun ScalarType.mapped(): String = when (this) {
//        ScalarType.Boolean -> "BOOL"
//        ScalarType.Byte -> "SMALLINT"
//        ScalarType.Short -> "SMALLINT"
//        ScalarType.Int -> "INT"
//        ScalarType.Long -> "BIGINT"
//        ScalarType.Float -> "REAL"
//        ScalarType.Double -> "DOUBLE PRECISION"
//        ScalarType.ShortString -> "VARCHAR(255)"
//        ScalarType.LongString -> "TEXT"
//        ScalarType.Date -> "TIMESTAMP"
//        is ScalarType.Enum -> "SMALLINT"
//    }
//
//    fun mapToKotlin(wraps: Scalar, result: ResultSet): Any? {
//        val type = wraps.type
//        return when (type) {
//            ScalarType.Boolean -> result.getBoolean(wraps.key)
//            ScalarType.Byte -> result.getByte(wraps.key)
//            ScalarType.Short -> result.getShort(wraps.key)
//            ScalarType.Int -> result.getInt(wraps.key)
//            ScalarType.Long -> result.getLong(wraps.key)
//            ScalarType.Float -> result.getFloat(wraps.key)
//            ScalarType.Double -> result.getDouble(wraps.key)
//            ScalarType.ShortString -> result.getString(wraps.key)
//            ScalarType.LongString -> result.getString(wraps.key)
//            ScalarType.Date -> result.getDate(wraps.key)
//            is ScalarType.Enum -> type.enum[result.getByte(wraps.key)]
//        }
//    }
//
//    fun mapFromKotlin(property: Scalar, value: Any?): String {
//        val type = property.type
//        return when (type) {
//            ScalarType.Boolean -> (value as Boolean).toString()
//            ScalarType.Byte -> (value as Byte).toString()
//            ScalarType.Short -> (value as Short).toString()
//            ScalarType.Int -> (value as Int).toString()
//            ScalarType.Long -> (value as Long).toString()
//            ScalarType.Float -> (value as Float).toString()
//            ScalarType.Double -> (value as Double).toString()
//            ScalarType.ShortString -> "'${value}'"
//            ScalarType.LongString -> "'${value}'"
//            ScalarType.Date -> Timestamp((value as Date).time).toString()
//            is ScalarType.Enum -> (value as ServerEnum.Value).value.toString()
//        }
//    }
//
//    fun createTable(table: Table) {
//        connection.createStatement().executeQuery(
//                """CREATE TABLE ${table.tableName}(
//                    ID INT  SERIAL PRIMARY KEY,""" +
//                        table.properties.values.joinToString(",\n") {
//                            it.run { "$key ${type.mapped()}" }
//                        } + """,
//                    PRIMARY KEY (ID)
//                )"""
//        )
//    }
//
//    override fun get(transaction: Transaction, id: String, read: Read): Instance? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun query(transaction: Transaction, read: Read): Collection<Instance> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun update(transaction: Transaction, write: Write): Instance {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun delete(transaction: Transaction, id: String): Boolean {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//
//    //    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
////        val result = connection.createStatement().executeQuery("SELECT id, ${properties.joinToString { it.name }} FROM ${table.tableName} WHERE id = ${id.toLong()}")
////        if (!result.next()) return null
////        return Instance(table, id, properties.associate {
////            it to mapToKotlin(it, result)
////        })
////    }
////
////    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
////        if (id != null) {
////            connection.createStatement().executeUpdate("UPDATE ${table.tableName} SET ${inProperties.entries.joinToString {
////                val type = it.key.type
////                it.key.name + " = " + mapFromKotlin(it.key, it.value)
////            }} WHERE id = ${id.toLong()};")
////            return get(table, id, outProperties)!!
////        } else {
////            val properties = inProperties.toList()
////            val statement = connection.createStatement()
////            statement.executeUpdate("INSERT INTO ${table.tableName} (${properties.joinToString { it.first.name }}) VALUES (${properties.joinToString {
////                mapFromKotlin(it.first, it.second)
////            }});")
////            val generatedKeys = statement.generatedKeys
////            generatedKeys.next()
////            return get(table, generatedKeys.getInt("id").toString(), outProperties)!!
////        }
////    }
//
//}