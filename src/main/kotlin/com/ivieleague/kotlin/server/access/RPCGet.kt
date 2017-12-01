package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.SClass
import com.ivieleague.kotlin.server.type.SNullable
import com.ivieleague.kotlin.server.type.SPointer
import com.ivieleague.kotlin.server.type.TypedObject

class RPCGet<T>(val dao: DAO<T>) : RPCMethod {
    override val description: String = "Get the ${dao.type.name} value at the id"

    val argumentPointer = RPCMethod.Argument(
            key = "pointer",
            description = "The pointer to the resource you want.",
            type = SPointer[dao.type]
    )

    override val arguments = listOf(argumentPointer)

    override val returns = RPCMethod.Returns(
            description = "The value at the location.",
            type = SNullable[dao.type]
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val pointer = arguments[argumentPointer]!!
        val result = dao.get(transaction, pointer)

        val type = dao.type
        if (type is SClass) {
            type.assertReadSecure(transaction, result as TypedObject)
            return type.filterReadSecure(transaction, result as TypedObject)
        }
        return result
    }
}