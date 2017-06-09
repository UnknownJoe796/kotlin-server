package com.ivieleague.kotlin.server.sql

/**
 * DSL for SQL
 * Created by josep on 6/8/2017.
 */

abstract class KTopLevel {
    val builder = StringBuilder()
    abstract fun end(): String
}

sealed class KType() {
    abstract val string: String

    class KInteger() : KType() {
        override val string: String get() = "INTEGER"
    }

    class KBigInteger() : KType() {
        override val string: String get() = "BIGINT"
    }

    class KDecimal(val totalDigits: Int, val afterDecimal: Int) : KType() {
        override val string: String get() = "DECIMAL($totalDigits,$afterDecimal)"
    }

    class KBoolean() : KType() {
        override val string: String get() = "BOOLEAN"
    }

    class KReal() : KType() {
        override val string: String get() = "REAL"
    }

    class KFloat() : KType() {
        override val string: String get() = "FLOAT"
    }

    class KDate() : KType() {
        override val string: String get() = "DATE"
    }

    class KTime() : KType() {
        override val string: String get() = "TIME"
    }

    class KTimeStamp() : KType() {
        override val string: String get() = "TIMESTAMP"
    }

    class KVarChar(val length: Int) : KType() {
        override val string: String get() = "VARCHAR($length)"
    }
}

data class KColumn(val name: String, val type: KType)

class KCreateTable(val name: String) : KTopLevel() {
    init {
        builder.append("CREATE TABLE $name(")
    }


    override fun end(): String {
        builder.append(");")
        return builder.toString()
    }
}