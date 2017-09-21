package com.ivieleague.kotlin.server.sql

data class SQLDelete(
        val table: SQLTable,
        val where: SQLCondition
) {
    override fun toString(): String {
        return """
DELETE FROM ${table.name}
WHERE $where"""
    }
}