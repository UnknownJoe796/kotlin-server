package com.ivieleague.kotlin.server.type


data class TypeField<T>(
        val key: String,
        val description: String,
        val type: SType<T>,
        val defaultGenerator: () -> T
) {
    constructor(
            key: String,
            description: String,
            type: SType<T>
    ) : this(key, description, type, { type.default })

    constructor(
            key: String,
            description: String,
            type: SType<T>,
            default: T
    ) : this(key, description, type, { default })
}