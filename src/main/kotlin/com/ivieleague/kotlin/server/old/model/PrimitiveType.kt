package com.ivieleague.kotlin.server.old.model

import kotlin.reflect.KClass

sealed class PrimitiveType(val type: KClass<*>) {
    object Boolean : PrimitiveType(Boolean::class) {
        override fun toString() = "Boolean"
    }

    object Byte : PrimitiveType(Byte::class) {
        override fun toString() = "Byte"
    }

    object Short : PrimitiveType(Short::class) {
        override fun toString() = "Short"
    }

    object Int : PrimitiveType(Int::class) {
        override fun toString() = "Int"
    }

    object Long : PrimitiveType(Long::class) {
        override fun toString() = "Long"
    }

    object Float : PrimitiveType(Float::class) {
        override fun toString() = "Float"
    }

    object Double : PrimitiveType(Double::class) {
        override fun toString() = "Double"
    }

    object ShortString : PrimitiveType(String::class) {
        override fun toString() = "ShortString"
    }

    object LongString : PrimitiveType(String::class) {
        override fun toString() = "LongString"
    }

    object JSON : PrimitiveType(String::class) {
        override fun toString() = "JSON"
    }

    object Date : PrimitiveType(Date::class) {
        override fun toString() = "Date"
    }

    class Enum(val enum: ServerEnum) : PrimitiveType(ServerEnum.Value::class) {
        override fun toString() = enum.name
    }
}