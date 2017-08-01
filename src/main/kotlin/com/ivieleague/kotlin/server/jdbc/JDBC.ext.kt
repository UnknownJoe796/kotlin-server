package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.sql.SQLQuery
import com.ivieleague.kotlin.server.sql.SQLTable
import java.sql.Connection
import java.sql.ResultSet
import java.util.*
import kotlin.NoSuchElementException

fun Connection.query(query: SQLQuery): ResultSet = createStatement().executeQuery(query.toString())
fun Connection.create(table: SQLTable): Boolean = createStatement().execute(table.toDefineString())

fun ResultSet.asIterator() = object : Iterator<ResultSet> {

    var nextChecked = false
    var hasNext = false

    override fun hasNext(): Boolean {
        if (nextChecked) return hasNext
        hasNext = this@asIterator.next()
        nextChecked = true
        return hasNext
    }

    override fun next(): ResultSet {
        if (!hasNext()) throw NoSuchElementException()
        nextChecked = false
        return this@asIterator
    }

}

data class JDBCTableInformation(
        val catalog: String,
        val schema: String,
        val name: String,
        val type: String,
        val remarks: String,
        val catalogType: String,
        val schemaType: String,
        val typeName: String,
        val selfReferencingColumnName: String,
        val refGeneration: String,
        val columnInformation: Collection<JDBCColumnInformation>,
        val primaryKeys: Collection<JDBCColumnInformation>
)

data class JDBCColumnInformation(
        val name: String, //4
        val type: Int, //5
        val typeName: String, //6
        val size: Int, //7
        val decimalDigits: Int, //9
        val radix: Int?, //10
        val nullable: Int, //11
        val remarks: String, //12
        val default: String, //13
        val charMax: Int, //16
        val ordinalPosition: Int, //17
        val isAutoIncrement: Boolean, //23
        val isGeneratedColumn: Boolean //24
)

val Connection_tables = WeakHashMap<Connection, List<JDBCTableInformation>>()
val Connection.tables: List<JDBCTableInformation>
    get() = Connection_tables.getOrPut(this) {
        getTablesInformation()
    }

fun Connection.getTablesInformation(): List<JDBCTableInformation> {
    val metadata = metaData
    val set = metadata.getTables(null, null, "%", null)
    return set.asIterator().asSequence().map {
        val catalog = set.getString(1)
        val schema = set.getString(2)
        val name = set.getString(3)
        JDBCTableInformation(
                catalog = catalog,
                schema = schema,
                name = name,
                type = set.getString(4),
                remarks = set.getString(5),
                catalogType = set.getString(6),
                schemaType = set.getString(7),
                typeName = set.getString(8),
                selfReferencingColumnName = set.getString(9),
                refGeneration = set.getString(10),
                columnInformation = metaData.getColumns(catalog, schema, name, null).asIterator().asSequence().map {
                    JDBCColumnInformation(
                            name = it.getString(4),
                            type = it.getInt(5),
                            typeName = it.getString(6),
                            size = it.getInt(7),
                            decimalDigits = it.getInt(9),
                            radix = it.getInt(10),
                            nullable = it.getInt(11),
                            remarks = it.getString(12),
                            default = it.getString(13),
                            charMax = it.getInt(16),
                            ordinalPosition = it.getInt(17),
                            isAutoIncrement = it.getString(23) == "YES",
                            isGeneratedColumn = it.getString(24) == "YES"
                    )
                }.toList(),
                primaryKeys = metadata.getPrimaryKeys(catalog, schema, name).asIterator().asSequence().map {
                    JDBCColumnInformation(
                            name = it.getString(4),
                            type = it.getInt(5),
                            typeName = it.getString(6),
                            size = it.getInt(7),
                            decimalDigits = it.getInt(9),
                            radix = it.getInt(10),
                            nullable = it.getInt(11),
                            remarks = it.getString(12),
                            default = it.getString(13),
                            charMax = it.getInt(16),
                            ordinalPosition = it.getInt(17),
                            isAutoIncrement = it.getString(23) == "YES",
                            isGeneratedColumn = it.getString(24) == "YES"
                    )
                }.toList()//TODO: Close ResultSet
        )
    }.toList()
}