package com.ivieleague.kotlin.server.sql

class SQLDirectColumn(val column: SQLColumn) : SQLExpression {
    override fun toString(): String = column.name
}