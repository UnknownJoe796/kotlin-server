package com.ivieleague.kotlin.server.sql

data class SQLDelete(
        val table: SQLTable,
        val where: SQLCondition
) : SQLExecutable {
    override fun toString(): String {
        return """
DELETE FROM ${table.name}
WHERE $where"""
    }
}