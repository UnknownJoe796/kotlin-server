package com.ivieleague.kotlin.server.sql

data class SQLUpsert(
        val table: SQLTable,
        val values: Map<SQLColumn, SQLLiteral>
) : SQLDataSource, SQLExecutable {
    override fun toString(): String {
        val entriesOrdered = values.entries.toList()
        return """
INSERT INTO ${table.name} (${entriesOrdered.joinToString { it.key.name }})
VALUES (${entriesOrdered.joinToString { it.value.toString() }})
ON CONFLICT DO UPDATE
SET ${entriesOrdered.joinToString { it.key.name + " = " + it.value.toString() }}
"""

    }
}