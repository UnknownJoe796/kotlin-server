package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.SimpleTypedObject


interface RPCMethod {
    val description: String
    val arguments: List<Argument>
    val returns: Returns
    val potentialExceptions: Map<Int, PotentialException<*>>
    val deprecated: Boolean get() = false

    data class Argument(
            val key: String,
            val description: String,
            val type: SType<*>
    )

    data class Returns(
            val description: String,
            val type: SType<*>
    )

    data class PotentialException<T : Any>(
            val code: Int,
            val name: String,
            val description: String,
            val type: SType<T>
    ) {
        fun exception(data: T? = null, message: String = description) = RPCException(RPCError(
                code = code,
                message = message,
                data = data
        ))
    }

    @Throws(RPCException::class)
    operator fun invoke(user: SimpleTypedObject?, arguments: Map<String, Any?>): Any?
}

