package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod
import com.ivieleague.kotlin.server.rpc.Transaction
import com.ivieleague.kotlin.server.rpc.get
import com.ivieleague.kotlin.server.type.*

class RPCQuery<T>(val dao: QueryableDAO<T>) : RPCMethod {
    override val description: String = "Query for values of type ${dao.type.name}"

    init {
        assert(dao.type is SClass)
    }

    val sortType = SSort[dao.type as SClass]

    val argumentCondition = RPCMethod.Argument(
            key = "condition",
            description = "The condition to query for.",
            type = SCondition[dao.type as SClass]
    )
    val argumentSort = RPCMethod.Argument(
            key = "sort",
            description = "The sort to query for.",
            type = SList[sortType]
    )
    val argumentCount = RPCMethod.Argument(
            key = "count",
            description = "The count to query for.",
            type = SInt
    )
    val argumentStart = RPCMethod.Argument(
            key = "start",
            description = "The start to query for.",
            type = dao.type
    )

    override val arguments = listOf(
            argumentCondition,
            argumentSort,
            argumentCount,
            argumentStart
    )

    override val returns = RPCMethod.Returns(
            description = "The pointer to the new value.",
            type = SPointer[dao.type]
    )

    override val potentialExceptions: Map<Int, RPCMethod.PotentialException<*>> = mapOf()

    override fun invoke(transaction: Transaction, arguments: Map<String, Any?>): Any? {
        val condition = arguments[argumentCondition]!!
        val sort = arguments[argumentSort]!!.mapNotNull { it?.let { sortType.breakApart[it] } }
        val count = arguments[argumentCount]!!
        val start = arguments[argumentStart]

        val results = dao.query(
                transaction = transaction,
                condition = condition,
                sort = sort,
                count = count,
                start = start
        )
        val type = dao.type
        if (type is SClass) {
            return results.mapNotNull {
                if (type.checkReadSecure(transaction, it as TypedObject))
                    type.filterReadSecure(transaction, it as TypedObject)
                else
                    null
            }
        }
        return results
    }
}