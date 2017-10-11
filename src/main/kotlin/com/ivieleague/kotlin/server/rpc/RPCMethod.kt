package com.ivieleague.kotlin.server.rpc

interface RPCMethod {
    val description: String
    val arguments: List<Argument>
    val returns: Returns

    operator fun invoke(user: Any?, arguments: List<Any?>): Any?
    operator fun invoke(user: Any?, arguments: Map<String, Any?>): Any?

    data class Returns(
            val type: Class<*>,
            val description: String,
            val isNullable: Boolean
    )

    data class Argument(
            val key: String,
            val type: Class<*>,
            val description: String,
            val optional: Boolean,
            val isNullable: Boolean
    )
}

