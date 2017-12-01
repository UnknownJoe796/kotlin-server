package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SType


interface RPCMethod {
    val description: String
    val arguments: List<Argument<*>>
    val returns: Returns<*>
    val potentialExceptions: Map<Int, PotentialException<*>>
    val deprecated: Boolean get() = false

    data class Argument<T>(
            val key: String,
            val description: String,
            val type: SType<T>
    )

    data class Returns<T>(
            val description: String,
            val type: SType<T>
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
    operator fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any?
}

operator fun <T> Map<String, Any?>.get(argument: RPCMethod.Argument<T>): T? = get(argument.key) as? T