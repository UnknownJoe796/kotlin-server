package com.ivieleague.kotlin.server.sql


sealed class SQLLiteral : SQLExpression {
    class LInteger(val value: Long) : SQLLiteral() {
        override fun toString(): String = value.toString()
    }

    class LReal(val value: Double) : SQLLiteral() {
        override fun toString(): String = value.toString()
    }

    object LTrue : SQLLiteral() {
        override fun toString(): String = "TRUE"
    }

    object LFalse : SQLLiteral() {
        override fun toString(): String = "FALSE"
    }

    object LNull : SQLLiteral() {
        override fun toString(): String = "NULL"
    }

    class LString(val value: String) : SQLLiteral() {
        //Whitelist over blacklist
        val escaped = value.replace(Regex("[^a-zA-Z 0-9,.+=-_/<>!@#$%^&*()`~]"), { it.value.toByteArray().joinToString("") { "\\x" + it.toString(16) } })

        override fun toString(): String = "E'$escaped'"
    }

    class LList(val values: List<SQLLiteral>) : SQLLiteral() {
        override fun toString(): String = values.joinToString(",", "(", ")")
    }
}

fun Any?.toSQLLiteral(): SQLLiteral = when (this) {
    null -> SQLLiteral.LNull
    true -> SQLLiteral.LTrue
    false -> SQLLiteral.LFalse
    is String -> SQLLiteral.LString(this)
    is Byte -> SQLLiteral.LInteger(this.toLong())
    is Short -> SQLLiteral.LInteger(this.toLong())
    is Int -> SQLLiteral.LInteger(this.toLong())
    is Long -> SQLLiteral.LInteger(this)
    is Float -> SQLLiteral.LReal(this.toDouble())
    is Double -> SQLLiteral.LReal(this)
    else -> throw IllegalArgumentException()
}