package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.model.*

class RPCModelDelete(
        val tableAccess: TableAccess,
        val schema: Schema
) : RPCMethod {
    override val description: String = "Deletes an item from the table ${tableAccess.table.tableName}"
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "id",
                    type = String::class.java,
                    description = "The id of the object to retrieve.",
                    optional = false,
                    isNullable = false
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            type = WriteResult::class.java,
            description = "The result of writing the changes.  Will contain only IDs.",
            isNullable = false
    )

    override fun invoke(user: Any?, arguments: List<Any?>): Any? = invoke(user, arguments[0] as String)
    override fun invoke(user: Any?, arguments: Map<String, Any?>): Any? = invoke(user, arguments["id"] as String)

    fun invoke(user: Any?, id: String): WriteResult = Transaction(user as? Instance, tableAccesses = schema, readOnly = false).use { txn ->
        tableAccess.update(txn, Write(id = id, delete = true))
    }
}