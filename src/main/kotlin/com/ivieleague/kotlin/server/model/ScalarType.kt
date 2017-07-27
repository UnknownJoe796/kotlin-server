package com.ivieleague.kotlin.server.model

import kotlin.reflect.KClass

sealed class ScalarType(val type: KClass<*>) {
    object Boolean : ScalarType(Boolean::class) {
        override fun toString() = "Boolean"
    }

    object Byte : ScalarType(Byte::class) {
        override fun toString() = "Byte"
    }

    object Short : ScalarType(Short::class) {
        override fun toString() = "Short"
    }

    object Int : ScalarType(Int::class) {
        override fun toString() = "Int"
    }

    object Long : ScalarType(Long::class) {
        override fun toString() = "Long"
    }

    object Float : ScalarType(Float::class) {
        override fun toString() = "Float"
    }

    object Double : ScalarType(Double::class) {
        override fun toString() = "Double"
    }

    object ShortString : ScalarType(String::class) {
        override fun toString() = "ShortString"
    }

    object LongString : ScalarType(String::class) {
        override fun toString() = "LongString"
    }

    object JSON : ScalarType(String::class) {
        override fun toString() = "JSON"
    }

    object Date : ScalarType(Date::class) {
        override fun toString() = "Date"
    }

    class Enum(val enum: ServerEnum) : ScalarType(ServerEnum.Value::class) {
        override fun toString() = enum.name
    }
}