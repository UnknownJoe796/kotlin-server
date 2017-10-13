package com.ivieleague.kotlin.server.old

import com.ivieleague.kotlin.server.old.model.*
import com.ivieleague.kotlin.server.old.type.Instance
import com.ivieleague.kotlin.server.old.type.Primitive
import com.ivieleague.kotlin.server.old.type.Property
import com.ivieleague.kotlin.server.rpc.RPCMethod

class RPCModelDelete(
        val tableAccess: TableAccess,
        val schema: Schema
) : RPCMethod {
    override val description: String = "Deletes an item from the table ${tableAccess.table.tableName}"
    override val arguments: List<Property> = listOf(
            Primitive(
                    key = "id",
                    type = PrimitiveType.ShortString,
                    description = "The id of the object to retrieve."
            )
    )
    override val returns: Property = Primitive(
            key = "id",
            type = PrimitiveType.ShortString,
            description = "The id of the object to deleted."
    )

    override fun invoke(user: Any?, arguments: List<Any?>): Any? = invoke(user, arguments[0] as String)
    override fun invoke(user: Any?, arguments: Map<String, Any?>): Any? = invoke(user, arguments["id"] as String)

    fun invoke(user: Any?, id: String): WriteResult = Transaction(user as? Instance, tableAccesses = schema, readOnly = false).use { txn ->
        tableAccess.update(txn, Write(id = id, delete = true))
    }
}