package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.type.SList
import com.ivieleague.kotlin.server.type.TypedObject

class RPCUpdate(val dao: DAO) : RPCMethod {
    override val description: String = "Update for ${dao.type}."
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "write",
                    description = "What to write",
                    type = SWrite[dao.type]
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            description = "The instances matching your query",
            type = SList(dao.type)
    )
    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = listOf<RPCMethod.PotentialException<*>>(
            DAOPotentialExceptions.notAllowedWrite(dao.type),
            DAOPotentialExceptions.notAllowedWriteOver(dao.type)
    ).associate { it.code to it }

    override fun invoke(user: TypedObject?, arguments: Map<String, Any?>): TypedObject
            = Transaction(user, readOnly = false).use {
        dao.update(it, arguments["write"] as TypedObject)
    }
}