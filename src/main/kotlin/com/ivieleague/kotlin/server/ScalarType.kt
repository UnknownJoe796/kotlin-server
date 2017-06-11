package com.ivieleague.kotlin.server

import kotlin.reflect.KClass

sealed class ScalarType(val type: KClass<*>) {
    object Boolean : ScalarType(Boolean::class)
    object Byte : ScalarType(Byte::class)
    object Short : ScalarType(Short::class)
    object Int : ScalarType(Int::class)
    object Long : ScalarType(Long::class)
    object Float : ScalarType(Float::class)
    object Double : ScalarType(Double::class)
    object ShortString : ScalarType(String::class)
    object LongString : ScalarType(String::class)
    object Date : ScalarType(Date::class)
    class Enum(val enum: ServerEnum) : ScalarType(ServerEnum.Value::class)
}