package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.access.assertModifySecure
import com.ivieleague.kotlin.server.access.assertWriteSecure
import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SVoid
import com.ivieleague.kotlin.server.type.TypedObject

class RPCSingletonSet<T>(val access: SingletonAccess<T>) : RPCMethod {
    override val description: String = "Get the value of ${access.type.name}"

    val argumentValue = RPCMethod.Argument(
            key = "value",
            description = "The value to insert.",
            type = access.type
    )
    override val arguments = listOf(argumentValue)

    override val returns = RPCMethod.Returns(
            description = "Nothing.",
            type = SVoid
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val value = arguments[argumentValue]!!
        val type = access.type
        if (type is SClass) {
            type.assertModifySecure(transaction) { access.get(transaction) as TypedObject }
            type.assertWriteSecure(transaction, value as TypedObject)
        }
        return access.set(transaction, arguments[argumentValue.key] as T)
    }
}