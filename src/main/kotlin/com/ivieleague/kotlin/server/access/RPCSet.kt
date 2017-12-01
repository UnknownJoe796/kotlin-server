package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SPointer
import com.ivieleague.kotlin.server.type.TypedObject

class RPCSet<T>(val dao: DAO<T>) : RPCMethod {
    override val description: String = "Sets the value of type ${dao.type.name} at the pointer."

    val argumentPointer = RPCMethod.Argument(
            key = "pointer",
            description = "The pointer to the resource you want.",
            type = SPointer[dao.type]
    )
    val argumentValue = RPCMethod.Argument(
            key = "value",
            description = "The value to insert.",
            type = dao.type
    )

    override val arguments = listOf(argumentPointer, argumentValue)

    override val returns = RPCMethod.Returns(
            description = "The pointer to the new value.",
            type = SPointer[dao.type]
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val value = arguments[argumentValue]!!
        val pointer = arguments[argumentPointer]!!
        val type = dao.type
        if (type is SClass) {
            type.assertModifySecure(transaction) { dao.get(transaction, pointer) as TypedObject }
            type.assertWriteSecure(transaction, value as TypedObject)
        }
        return dao.set(transaction, pointer, value)
    }
}