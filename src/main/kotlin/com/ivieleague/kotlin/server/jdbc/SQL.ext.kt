package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.Fetcher
import com.ivieleague.kotlin.server.model.*
import com.ivieleague.kotlin.server.sql.SQLColumn
import com.ivieleague.kotlin.server.sql.SQLDataType
import com.ivieleague.kotlin.server.sql.SQLTable

fun ScalarType.toSql(): SQLDataType = when (this) {
    ScalarType.Boolean -> SQLDataType.SQLBool
    ScalarType.Byte -> SQLDataType.SQLShort
    ScalarType.Short -> SQLDataType.SQLShort
    ScalarType.Int -> SQLDataType.SQLInt
    ScalarType.Long -> SQLDataType.SQLLong
    ScalarType.Float -> SQLDataType.SQLReal
    ScalarType.Double -> SQLDataType.SQLDouble
    ScalarType.ShortString -> SQLDataType.SQLVarchar(255)
    ScalarType.LongString -> SQLDataType.SQLText
    ScalarType.JSON -> SQLDataType.SQLJson
    ScalarType.Date -> SQLDataType.SQLTimestamp
    is ScalarType.Enum -> SQLDataType.SQLShort
}

fun Scalar.toSql(): SQLColumn = SQLColumn(
        name = key,
        description = description,
        type = type.toSql(),
        modifiers = listOf()
)

fun Link.toSql(accesses: Fetcher<Table, TableAccess>): List<SQLColumn> {
    val otherTable = (accesses[table] as? SQLTableAccess)?.sqlTable
    val otherPrimaryKeys = otherTable?.primaryKey
    val ownedColumns = otherPrimaryKeys?.map {
        SQLColumn(
                "owned_${it.name}",
                "The owner of this relationship",
                it.type,
                modifiers = listOf(
                        SQLColumn.Modifier.ForeignKey(otherTable.name, it.name)
                )
        )
    } ?: listOf(SQLColumn(
            "owned",
            "The owner of this relationship",
            type = SQLDataType.SQLVarchar(255),
            modifiers = listOf(
                    SQLColumn.Modifier.Unique
            )
    ))
    return ownedColumns
}

fun Multilink.toSql(thisTable: SQLTable, accesses: Fetcher<Table, TableAccess>): SQLTable {
    val otherTable = (accesses[table] as? SQLTableAccess)?.sqlTable
    val otherPrimaryKeys = otherTable?.primaryKey
    val ownerColumns = thisTable.primaryKey.map {
        SQLColumn(
                "owner_${it.name}",
                "The owner of this relationship",
                it.type,
                modifiers = listOf(
                        SQLColumn.Modifier.ForeignKey(thisTable.name, it.name)
                )
        )
    }
    val ownedColumns = otherPrimaryKeys?.map {
        SQLColumn(
                "owned_${it.name}",
                "The owned of this relationship",
                it.type,
                modifiers = listOf(
                        SQLColumn.Modifier.ForeignKey(otherTable.name, it.name)
                )
        )
    } ?: listOf(SQLColumn(
            "owned",
            "The owned of this relationship",
            type = SQLDataType.SQLVarchar(255),
            modifiers = listOf(
                    SQLColumn.Modifier.Unique
            )
    ))
    return SQLTable(
            name = "${thisTable.name}_${this.key}",
            description = this.description,
            columns = ownerColumns + ownedColumns,
            primaryKey = ownerColumns + ownedColumns
    )
}

val StandardPrimaryKey = SQLColumn(
        "id",
        "The unique ID of this item",
        SQLDataType.SQLIntSerial,
        modifiers = listOf(
                SQLColumn.Modifier.PrimaryKey
        )
)

fun Table.toPrimaryKeySql(): SQLTable {
    return SQLTable(
            name = this.tableName,
            description = this.tableDescription,
            columns = listOf(StandardPrimaryKey),
            primaryKey = listOf(StandardPrimaryKey)
    )
}

fun Table.toSql(accesses: Fetcher<Table, TableAccess>): SQLTable {
    return SQLTable(
            name = this.tableName,
            description = this.tableDescription,
            columns = listOf(StandardPrimaryKey) + scalars.map { it.toSql() } + links.flatMap { it.toSql(accesses) },
            primaryKey = listOf(StandardPrimaryKey)
    )
}

fun Table.toRelationshipSql(thisTable: SQLTable, accesses: Fetcher<Table, TableAccess>): List<SQLTable> {
    return multilinks.map { it.toSql(thisTable, accesses) }
}