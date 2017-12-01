package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SPointer
import com.ivieleague.kotlin.server.type.SVoid
import com.ivieleague.kotlin.server.type.TypedObject

class RPCDelete<T>(val dao: DAO<T>) : RPCMethod {
    override val description: String = "Deletes the ${dao.type.name} value at the pointer."

    val argumentPointer = RPCMethod.Argument(
            key = "pointer",
            description = "The pointer to the resource you want.",
            type = SPointer[dao.type]
    )

    override val arguments = listOf(argumentPointer)

    override val returns = RPCMethod.Returns(
            description = "",
            type = SVoid
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val pointer = arguments[argumentPointer]!!

        val type = dao.type
        if (type is SClass) {
            type.assertModifySecure(transaction) { dao.get(transaction, pointer) as TypedObject }
        }
        return dao.delete(transaction, pointer)
    }
}