package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.model.*
import com.ivieleague.kotlin.server.type.Instance

class RPCModelUpdate(
        val tableAccess: TableAccess,
        val schema: Schema
) : RPCMethod {
    override val description: String = "Update the table ${tableAccess.table.tableName}"
    override val arguments: List<RPCMethod.Argument> = listOf(
            RPCMethod.Argument(
                    key = "data",
                    type = Write::class.java,
                    description = "The modifications to make.",
                    optional = false,
                    isNullable = false
            )
    )
    override val returns: RPCMethod.Returns = RPCMethod.Returns(
            type = WriteResult::class.java,
            description = "The result of writing the changes.  Will contain only IDs.",
            isNullable = false
    )

    override fun invoke(user: Any?, arguments: List<Any?>): Any? = invoke(user, arguments[0] as Write)
    override fun invoke(user: Any?, arguments: Map<String, Any?>): Any? = invoke(user, arguments["data"] as Write)

    fun invoke(user: Any?, write: Write): WriteResult = Transaction(user as? Instance, tableAccesses = schema, readOnly = false).use { txn ->
        tableAccess.update(txn, write)
    }
}