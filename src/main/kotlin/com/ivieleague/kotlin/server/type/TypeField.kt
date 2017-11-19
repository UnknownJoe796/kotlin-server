package com.ivieleague.kotlin.server.type


data class TypeField<T>(
        val key: String,
        val description: String,
        val type: SType<T>,
        val default: T? = null
)