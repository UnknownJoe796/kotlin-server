package com.ivieleague.kotlin.server.sql

sealed class SQLCondition : SQLExpression {
    class And(val conditions: List<SQLCondition>) {
        override fun toString(): String = conditions.joinToString(" AND ", "(", ")")
    }

    class Or(val conditions: List<SQLCondition>) {
        override fun toString(): String = conditions.joinToString(" OR ", "(", ")")
    }

    class Equal(val left: SQLExpression, val right: SQLExpression) {
        override fun toString(): String = "$left = $right"
    }
}