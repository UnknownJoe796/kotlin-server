package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.Property

interface RPCMethod {
    val description: String
    val arguments: List<Property>
    val returns: Property

    operator fun invoke(user: Any?, arguments: List<Any?>): Any?
    operator fun invoke(user: Any?, arguments: Map<String, Any?>): Any?
}

