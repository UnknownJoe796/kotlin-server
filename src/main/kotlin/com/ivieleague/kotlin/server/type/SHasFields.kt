package com.ivieleague.kotlin.server.type

interface SHasFields<T : Any> : SType<T> {
    val fields: Map<String, TypeField<*>>
}