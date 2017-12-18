package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.access.SPartialClass
import com.ivieleague.kotlin.server.access.assertModifySecure
import com.ivieleague.kotlin.server.access.assertPartialWriteSecure
import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SVoid
import com.ivieleague.kotlin.server.type.TypedObject

class RPCSingletonModify<T, M>(val access: SingletonModifyAccess<T, M>) : RPCMethod {
    override val description: String = "Get the value of ${access.type.name}"

    val argumentValue = RPCMethod.Argument(
            key = "value",
            description = "The values to modify.",
            type = access.modifyType
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
        val modType = access.modifyType
        if (type is SClass && modType is SClass) {
            type.assertModifySecure(transaction) { access.get(transaction) as TypedObject }
            if (type is SPartialClass) {
                type.assertPartialWriteSecure(transaction, value as TypedObject)
            }
        }
        return access.modify(transaction, arguments[argumentValue.key] as M)
    }
}