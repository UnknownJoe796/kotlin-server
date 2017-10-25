package com.ivieleague.kotlin.server.sql

class SQLResultColumn(val source: SQLDataSourceAccess, val column: SQLColumn, val alias: String? = null) : SQLExpression {
    fun toDefineString(): String = if (alias != null) "($source.$column) AS $alias" else "($source.$column)"
    override fun toString(): String = alias ?: "$source.$column"
}