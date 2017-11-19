package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SMap

class GetMethodsRPCMethod(methods: Map<String, RPCMethod>) : RPCMethod {

    val methodData by lazy {
        methods.mapValues {
            SRPCMethod.make(it.value)
        }
    }

    override val description: String = "Retrieves all of the available methods."
    override val arguments: List<RPCMethod.Argument<*>> = listOf()
    override val returns: RPCMethod.Returns<*> = RPCMethod.Returns(
            description = "A map of all the types used.",
            type = SMap[SRPCMethod]
    )
    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        return methodData
    }

}