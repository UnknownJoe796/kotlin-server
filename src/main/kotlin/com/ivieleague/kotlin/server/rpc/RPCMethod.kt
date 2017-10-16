package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject


interface RPCMethod {
    val description: String
    val arguments: List<Argument>
    val returns: Returns

    data class Argument(
            val key: String,
            val description: String,
            val type: SType<*>
    )

    data class Returns(
            val description: String,
            val type: SType<*>
    )

    operator fun invoke(user: TypedObject?, arguments: Map<String, Any?>): Any?
}

