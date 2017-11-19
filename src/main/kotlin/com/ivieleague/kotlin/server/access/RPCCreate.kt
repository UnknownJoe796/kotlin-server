package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SPointer
import com.ivieleague.kotlin.server.type.TypedObject

class RPCCreate<T>(val dao: DAO<T>) : RPCMethod {
    override val description: String = "Create a new value of type ${dao.type.name}"

    val argumentValue = RPCMethod.Argument(
            key = "value",
            description = "The value to insert.",
            type = dao.type
    )

    override val arguments = listOf(argumentValue)

    override val returns = RPCMethod.Returns(
            description = "The pointer to the new value.",
            type = SPointer[dao.type]
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val value = arguments[argumentValue]!!
        val type = dao.type
        if (type is SClass) {
            type.assertWriteSecure(transaction, value as TypedObject)
        }
        return dao.create(transaction, value)
    }
}