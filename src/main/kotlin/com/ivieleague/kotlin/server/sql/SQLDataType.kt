package com.ivieleague.kotlin.server.sql

sealed class SQLDataType {
    object SQLBool : SQLDataType() {
        override fun toString() = "BOOL"
    }

    object SQLShort : SQLDataType() {
        override fun toString() = "SMALLINT"
    }

    object SQLInt : SQLDataType() {
        override fun toString() = "INT"
    }

    object SQLIntSerial : SQLDataType() {
        override fun toString() = "SERIAL"
    }

    object SQLLong : SQLDataType() {
        override fun toString() = "BIGINT"
    }

    object SQLReal : SQLDataType() {
        override fun toString() = "REAL"
    }

    object SQLDouble : SQLDataType() {
        override fun toString() = "DOUBLE PRECISION"
    }

    class SQLVarchar(val size: Int) : SQLDataType() {
        override fun toString() = "VARCHAR($size)"
    }

    object SQLText : SQLDataType() {
        override fun toString() = "TEXT"
    }

    object SQLJson : SQLDataType() {
        override fun toString() = "JSON"
    }

    object SQLTimestamp : SQLDataType() {
        override fun toString() = "TIMESTAMP"
    }
}