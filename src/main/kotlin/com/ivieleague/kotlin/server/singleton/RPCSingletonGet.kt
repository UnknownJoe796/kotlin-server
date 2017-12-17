package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.access.assertReadSecure
import com.ivieleague.kotlin.server.access.filterReadSecure
import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.TypedObject

class RPCSingletonGet<T>(val access: SingletonAccess<T>) : RPCMethod {
    override val description: String = "Get the value of ${access.type.name}"

    override val arguments = listOf<RPCMethod.Argument<*>>()

    override val returns = RPCMethod.Returns(
            description = "The value.",
            type = access.type
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val result = access.get(transaction)
        val type = access.type
        if (type is SClass) {
            type.assertReadSecure(transaction, result as TypedObject)
            return type.filterReadSecure(transaction, result as TypedObject)
        }
        return result
    }
}