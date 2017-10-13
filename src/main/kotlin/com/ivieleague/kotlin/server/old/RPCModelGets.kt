package com.ivieleague.kotlin.server.old

import com.ivieleague.kotlin.server.old.model.*
import com.ivieleague.kotlin.server.old.type.Instance
import com.ivieleague.kotlin.server.rpc.RPCMethod

class RPCModelGets(
        val tableAccess: TableAccess,
        val schema: Schema
) : RPCMethod {
    override val description: String = "Gets an item from the table ${tableAccess.table.tableName}"
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "id",
                    type = List::class.java,
                    description = "A list of the ids of the objects to retrieve.",
                    optional = false,
                    isNullable = false
            ),
            RPCMethod.Argument(
                    key = "read",
                    type = Read::class.java,
                    description = "The info about the object to retrieve.",
                    optional = false,
                    isNullable = false
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            type = Map::class.java,
            description = "A map of the ID of each object to the info requested.",
            isNullable = true
    )

    @Suppress("UNCHECKED_CAST")
    override fun invoke(user: Any?, arguments: List<Any?>): Any? = invoke(user, arguments[0] as List<String>, arguments[1] as Read)

    @Suppress("UNCHECKED_CAST")
    override fun invoke(user: Any?, arguments: Map<String, Any?>): Any? = invoke(user, arguments["id"] as List<String>, arguments["read"] as Read)

    fun invoke(user: Any?, ids: List<String>, read: Read) = Transaction(user as? Instance, tableAccesses = schema, readOnly = true).use { txn ->
        tableAccess.gets(txn, ids, read)
    }
}