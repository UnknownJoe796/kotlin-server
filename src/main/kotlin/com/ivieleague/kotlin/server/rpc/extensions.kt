package com.ivieleague.kotlin.server.rpc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ivieleague.kotlin.server.exceptionWrap
import com.ivieleague.kotlin.server.receiveJson
import com.ivieleague.kotlin.server.respondJson
import com.ivieleague.kotlin.server.type.TypedObject
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.post

private fun deserializeRPCRequestAndExecute(user: TypedObject?, mapper: ObjectMapper, tree: JsonNode, methods: Map<String, RPCMethod>): RPCResponse {
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
    val parametersNode = tree.get("parameters")

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
        RPCResponse(
                id,
                result = method.invoke(user, parameters)
        )
    } catch (e: RPCException) {
        RPCResponse(
                id,
                error = e.rpcError
        )
    }

}

fun Route.rpc(mapper: ObjectMapper, methods: Map<String, RPCMethod>, userGetter: (ApplicationCall) -> TypedObject? = { null }) {
    post() {
        exceptionWrap {
            val user = userGetter(it)
            try {
                val node = it.request.receiveJson<JsonNode>()!!
                if (node.isObject) {
                    val result = deserializeRPCRequestAndExecute(user, mapper, node, methods)
                    it.respondJson(result, if (result.error == null) HttpStatusCode.OK else HttpStatusCode.BadRequest)
                } else {
                    val results = node.elements().asSequence()
                            .map { deserializeRPCRequestAndExecute(user, mapper, it, methods) }
                            .toList()
                    it.respondJson(results)
                }
            } catch (e: Exception) {
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