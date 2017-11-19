package com.ivieleague.kotlin.server.rpc

import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.exceptionWrap
import com.ivieleague.kotlin.server.receiveJson
import com.ivieleague.kotlin.server.respondJson
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.SimpleTypedObject
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.post

private fun deserializeRPCRequestAndExecute(transaction: Transaction, tree: JsonNode, methods: Map<String, RPCMethod>): RPCResponse {
    val id = tree.get("id").asInt()

    val methodName = tree.get("method").asText()
    val method = methods[methodName] ?: return RPCResponse(
            id = id,
            error = RPCError(
                    code = RPCError.CODE_METHOD_NOT_FOUND,
                    message = "Method '$methodName' not found."
            )
    )

    val parameters = HashMap<String, Any?>()
    val parametersNode = tree.get("params") ?: return RPCResponse(
            id = id,
            error = RPCError(
                    code = RPCError.CODE_INVALID_PARAMS,
                    message = "Params not found."
            )
    )

    if (parametersNode.isObject) {
        for ((key, value) in parametersNode.fields()) {
            val argument = method.arguments.find { it.key == key } ?: return RPCResponse(
                    id = id,
                    error = RPCError(
                            code = RPCError.CODE_INVALID_PARAMS,
                            message = "No parameter '$key' found in method '$methodName'."
                    )
            )
            parameters[key] = argument.type.parse(value)
        }
    } else {
        parametersNode.elements().asSequence().forEachIndexed { index, value ->
            val argument = method.arguments.getOrNull(index) ?: return RPCResponse(
                    id = id,
                    error = RPCError(
                            code = RPCError.CODE_INVALID_PARAMS,
                            message = "Incorrect number of arguments for method '$methodName'."
                    )
            )
            parameters[argument.key] = argument.type.parse(value)
        }
    }

    return try {
        val result = method.invoke(transaction, parameters)
        RPCResponse(
                id,
                result = (method.returns.type as SType<Any?>).serialize(JsonGlobals.jsonNodeFactory, result)
        )
    } catch (e: RPCException) {
        RPCResponse(
                id,
                error = e.rpcError
        )
    }
}

fun Route.rpc(methods: Map<String, RPCMethod>, userGetter: (ApplicationCall) -> SimpleTypedObject? = { null }) {
    post() {
        exceptionWrap {
            val user = userGetter(it)
            try {
                val node = it.request.receiveJson<JsonNode>()!!
                if (node.isObject) {
                    val result = Transaction().use { txn ->
                        deserializeRPCRequestAndExecute(txn, node, methods)
                    }
                    it.respondJson(result, if (result.error == null) HttpStatusCode.OK else HttpStatusCode.BadRequest)

                } else {
                    val results = Transaction().use { txn ->
                        node.elements().asSequence()
                                .map { deserializeRPCRequestAndExecute(txn, it, methods) }
                                .toList()
                    }
                    it.respondJson(results)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                it.respondJson(RPCResponse(
                        id = 0,
                        error = RPCError(
                                RPCError.CODE_PARSE_ERROR,
                                e.message ?: "Unknown parsing exception"
                        )
                ))
            }
        }
    }
}