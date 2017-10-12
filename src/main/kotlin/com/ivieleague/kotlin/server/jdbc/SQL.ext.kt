package com.ivieleague.kotlin.server.jdbc

import com.ivieleague.kotlin.server.model.PrimitiveType
import com.ivieleague.kotlin.server.sql.SQLColumn
import com.ivieleague.kotlin.server.sql.SQLDataType
import com.ivieleague.kotlin.server.sql.SQLTable
import com.ivieleague.kotlin.server.type.Link
import com.ivieleague.kotlin.server.type.Multilink
import com.ivieleague.kotlin.server.type.Primitive
import com.ivieleague.kotlin.server.type.Table


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

fun PrimitiveType.toSql(): SQLDataType = when (this) {
    PrimitiveType.Boolean -> SQLDataType.SQLBool
    PrimitiveType.Byte -> SQLDataType.SQLShort
    PrimitiveType.Short -> SQLDataType.SQLShort
    PrimitiveType.Int -> SQLDataType.SQLInt
    PrimitiveType.Long -> SQLDataType.SQLLong
    PrimitiveType.Float -> SQLDataType.SQLReal
    PrimitiveType.Double -> SQLDataType.SQLDouble
    PrimitiveType.ShortString -> SQLDataType.SQLVarchar(255)
    PrimitiveType.LongString -> SQLDataType.SQLText
    PrimitiveType.JSON -> SQLDataType.SQLJson
    PrimitiveType.Date -> SQLDataType.SQLTimestamp
    is PrimitiveType.Enum -> SQLDataType.SQLShort
}

fun Primitive.toSql(): SQLColumn = SQLColumn(
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
        columns = listOf(StandardPrimaryKey) + primitives.map { it.toSql() } + links.map { it.toSql() },
        primaryKey = listOf(StandardPrimaryKey)
)

fun Table.toMultilinkTablesSql() = multilinks.associate {
    it to it.toSql(tableName)
}