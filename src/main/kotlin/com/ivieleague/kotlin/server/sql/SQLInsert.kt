package com.ivieleague.kotlin.server.sql

data class SQLInsert(
        val table: SQLTable,
        val values: Map<SQLColumn, SQLLiteral>
) : SQLDataSource, SQLExecutable {
    override fun toString(): String {
        val entriesOrdered = values.entries.toList()
        return """
INSERT INTO ${table.name} (${entriesOrdered.joinToString { it.key.name }})
VALUES (${entriesOrdered.joinToString { it.value.toString() }})"""
    }
}