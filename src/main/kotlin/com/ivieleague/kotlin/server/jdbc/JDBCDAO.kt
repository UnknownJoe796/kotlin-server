package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.*
import java.sql.Connection
import java.sql.ResultSet

class JDBCDAO(val connection: Connection, val database: String, val schema: Schema) : DAO {

    fun ServerType.mapped(): String = when (this) {
        ServerType.TBoolean -> "BOOL"
        ServerType.TByte -> "SMALLINT"
        ServerType.TShort -> "SMALLINT"
        ServerType.TInt -> "INT"
        ServerType.TLong -> "BIGINT"
        ServerType.TFloat -> "REAL"
        ServerType.TDouble -> "DOUBLE PRECISION"
        ServerType.TShortString -> "VARCHAR(255)"
        ServerType.TLongString -> "TEXT"
        is ServerType.TPointer -> "INT REFERENCES ${this.table.tableName}(id)"
        is ServerType.TListPointers -> TODO() //TODO: We need to create another table representing the relation
        is ServerType.TEnum -> "SMALLINT"
    }

    fun mapToKotlin(property: Property, result: ResultSet): Any? {
        val type = property.type
        return when (type) {
            ServerType.TBoolean -> result.getBoolean(property.name)
            ServerType.TByte -> result.getByte(property.name)
            ServerType.TShort -> result.getShort(property.name)
            ServerType.TInt -> result.getInt(property.name)
            ServerType.TLong -> result.getLong(property.name)
            ServerType.TFloat -> result.getFloat(property.name)
            ServerType.TDouble -> result.getDouble(property.name)
            ServerType.TShortString -> result.getString(property.name)
            ServerType.TLongString -> result.getString(property.name)
            is ServerType.TPointer -> result.getInt(property.name).toString()
            is ServerType.TListPointers -> TODO() //TODO: We need to create another table representing the relation
            is ServerType.TEnum -> result.getShort(property.name)
        }
    }

    fun mapFromKotlin(property: Property, value: Any?): String {
        val type = property.type
        return when (type) {
            ServerType.TBoolean -> (value as Boolean).toString()
            ServerType.TByte -> (value as Byte).toString()
            ServerType.TShort -> (value as Short).toString()
            ServerType.TInt -> (value as Int).toString()
            ServerType.TLong -> (value as Long).toString()
            ServerType.TFloat -> (value as Float).toString()
            ServerType.TDouble -> (value as Double).toString()
            ServerType.TShortString -> "'${value}'"
            ServerType.TLongString -> "'${value}'"
            is ServerType.TPointer -> (value as String).toInt().toString()
            is ServerType.TListPointers -> TODO() //TODO: We need to create another table representing the relation
            is ServerType.TEnum -> (value as Short).toString()
        }
    }

    fun createTable(table: Table) {
        connection.createStatement().executeQuery(
                """CREATE TABLE ${table.tableName}(
                    ID INT  SERIAL PRIMARY KEY,""" +
                        table.properties.values.joinToString(",\n") {
                            it.run { "$name ${type.mapped()}" }
                        } + """,
                    PRIMARY KEY (ID)
                )"""
        )
    }

    override fun get(table: Table, id: String, properties: Collection<Property>): Instance? {
        val result = connection.createStatement().executeQuery("SELECT id, ${properties.joinToString { it.name }} FROM ${table.tableName} WHERE id = ${id.toLong()}")
        if (!result.next()) return null
        return Instance(table, id, properties.associate {
            it to mapToKotlin(it, result)
        })
    }

    override fun set(table: Table, id: String?, inProperties: Map<Property, Any?>, outProperties: Collection<Property>): Instance {
        if (id != null) {
            connection.createStatement().executeUpdate("UPDATE ${table.tableName} SET ${inProperties.entries.joinToString {
                val type = it.key.type
                it.key.name + " = " + mapFromKotlin(it.key, it.value)
            }} WHERE id = ${id.toLong()};")
            return get(table, id, outProperties)!!
        } else {
            val properties = inProperties.toList()
            val statement = connection.createStatement()
            statement.executeUpdate("INSERT INTO ${table.tableName} (${properties.joinToString { it.first.name }}) VALUES (${properties.joinToString {
                mapFromKotlin(it.first, it.second)
            }});")
            val generatedKeys = statement.generatedKeys
            generatedKeys.next()
            return get(table, generatedKeys.getInt("id").toString(), outProperties)!!
        }
    }

    override fun query(table: Table, queryConditions: List<Condition>, outProperties: Collection<Property>): Collection<Instance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}