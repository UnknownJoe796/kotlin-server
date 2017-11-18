package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.type.SList
import com.ivieleague.kotlin.server.type.TypedObject

class RPCQuery(val dao: DAO) : RPCMethod {
    override val description: String = "Queries for ${dao.type}."
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "read",
                    description = "The fields to read from",
                    type = SRead[dao.type]
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            description = "The instances matching your query",
            type = SList[dao.type]
    )
    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = listOf<RPCMethod.PotentialException<*>>(
            DAOPotentialExceptions.notAllowedRead(dao.type)
    ).associate { it.code to it }

    override fun invoke(user: TypedObject?, arguments: Map<String, Any?>): List<TypedObject>
            = Transaction(user, readOnly = true).use {
        dao.query(it, arguments["read"] as TypedObject)
    }

}