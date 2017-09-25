package com.ivieleague.kotlin.server.sql

data class SQLUpdate(
        val table: SQLTable,
        val values: Map<SQLColumn, SQLLiteral>,
        val where: SQLCondition
) : SQLDataSource, SQLExecutable {
    override fun toString(): String {
        return """
UPDATE ${table.name}
SET ${values.entries.joinToString { it.key.name + " = " + it.value.toString() }}
WHERE $where"""
    }
}