package com.ivieleague.kotlin.server.old

import com.ivieleague.kotlin.server.old.model.*
import com.ivieleague.kotlin.server.old.type.Instance
import com.ivieleague.kotlin.server.rpc.RPCMethod

class RPCModelQuery(
        val tableAccess: TableAccess,
        val schema: Schema
) : RPCMethod {
    override val description: String = "Gets an item from the table ${tableAccess.table.tableName}"
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "read",
                    type = Read::class.java,
                    description = "The info about the object to retrieve.",
                    optional = false,
                    isNullable = false
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            type = Instance::class.java,
            description = "The information about the object requested, or null if no object is found.",
            isNullable = true
    )

    override fun invoke(user: Any?, arguments: List<Any?>): Any? = invoke(user, arguments[0] as Read)
    override fun invoke(user: Any?, arguments: Map<String, Any?>): Any? = invoke(user, arguments["read"] as Read)

    fun invoke(user: Any?, read: Read) = Transaction(user as? Instance, tableAccesses = schema, readOnly = true).use { txn ->
        tableAccess.query(txn, read)
    }
}