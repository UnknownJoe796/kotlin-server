package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.type.SMap
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject
import com.ivieleague.kotlin.server.type.meta.STypeInterface

class GetTypesRPCMethod(typesGetter: () -> Map<String, SType<*>>) : RPCMethod {

    val typeData by lazy {
        typesGetter().mapValues { it.value.reflect() }
    }

    override val description: String = "Retrieves all of the types used and returns them in a map."
    override val arguments: List<RPCMethod.Argument> = listOf()
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            description = "A map of all the types used.",
            type = SMap[STypeInterface]
    )
    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(user: TypedObject?, arguments: Map<String, Any?>): Any? = typeData

    constructor(methods: Map<String, RPCMethod>) : this({
        getTypesRecursively(methods.values).associate { it.name to it }
    })

    companion object {
        fun getTypesRecursively(methods: Collection<RPCMethod>): ArrayList<SType<*>> {

            val output = ArrayList<SType<*>>()
            val completed = HashSet<SType<*>>()
            val queue = HashSet<SType<*>>()
            for (method in methods) {
                for (argument in method.arguments) {
                    val type = argument.type
                    if (type !in completed)
                        queue.add(type)
                }
                val type = method.returns.type
                if (type !in completed)
                    queue.add(type)
            }
            while (queue.isNotEmpty()) {
                val current = queue.first()
                if (current in completed) continue
                queue.remove(current)
                output.add(current)
                for (type in current.dependencies) {
                    if (type !in completed)
                        queue.add(type)
                }
            }

            return output
        }
    }
}

