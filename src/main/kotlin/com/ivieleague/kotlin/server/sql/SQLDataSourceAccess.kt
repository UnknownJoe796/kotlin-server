package com.ivieleague.kotlin.server.sql

class SQLDataSourceAccess(val dataSource: SQLDataSource, val alias: String) {
    fun toDefineString(): String = "($dataSource) $alias"
    override fun toString(): String = alias
}