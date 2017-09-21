package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.model.*
import com.ivieleague.kotlin.server.sql.SQLColumn
import com.ivieleague.kotlin.server.sql.SQLDataType
import com.ivieleague.kotlin.server.sql.SQLTable


val StandardPrimaryKey = SQLColumn(
        "_id",
        "The unique ID of this item",
        SQLDataType.SQLIntSerial,
        modifiers = listOf(
                SQLColumn.Modifier.PrimaryKey
        )
)
val StandardOwnerKey = SQLColumn(
        "owner",
        "The owner of this relationship",
        type = SQLDataType.SQLLong,
        modifiers = listOf()
)
val StandardOwnedKey = SQLColumn(
        "owned",
        "The thing owned through this relationship",
        type = SQLDataType.SQLVarchar(255),
        modifiers = listOf()
)

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

fun Link.toSql(): SQLColumn {
    return SQLColumn(
            name = key,
            description = description,
            type = SQLDataType.SQLVarchar(255),
            modifiers = listOf()
    )
}

fun Multilink.toSql(prefix: String): SQLTable {
    val columns = listOf(StandardOwnedKey, StandardOwnedKey)
    return SQLTable(
            name = "${prefix}_${this.key}",
            description = this.description,
            columns = columns,
            primaryKey = columns
    )
}

fun Table.toSql() = SQLTable(
        name = tableName,
        description = tableDescription,
        columns = listOf(StandardPrimaryKey) + scalars.map { it.toSql() } + links.map { it.toSql() },
        primaryKey = listOf(StandardPrimaryKey)
)

fun Table.toMultilinkTablesSql() = multilinks.associate {
    it to it.toSql(tableName)
}