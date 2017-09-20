package com.ivieleague.kotlin.server.sql

data class SQLTable(
        val catalog: String? = null,
        val schema: String? = null,
        val name: String,
        val description: String,
        val columns: List<SQLColumn>,
        val primaryKey: List<SQLColumn>
) : SQLDataSource {
    fun toDefineString(): String = """CREATE TABLE $name(
    ${columns.joinToString { it.toDefineString() }}
    PRIMARY KEY (${primaryKey.joinToString { it.name }}
);"""

    fun toDefineIfNotExistsString(): String = """CREATE TABLE IF NOT EXISTS $name(
    ${columns.joinToString { it.toDefineString() }}
    PRIMARY KEY (${primaryKey.joinToString { it.name }}
);"""
}