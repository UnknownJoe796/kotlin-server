package com.ivieleague.kotlin.server.rpc

import com.fasterxml.jackson.databind.JsonNode
import com.ivieleague.kotlin.server.JsonGlobals
import com.ivieleague.kotlin.server.getContentJson
import com.ivieleague.kotlin.server.handler.handler
import com.ivieleague.kotlin.server.respond
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


fun rpc(
        methods: Map<String, RPCMethod>,
        userGetter: (HttpServletRequest) -> TypedObject? = { null }
) = handler { target, baseRequest, request, response ->
    if (target != "/rpc") return@handler
    val user = userGetter.invoke(request)
    try {
        val node = request.getContentJson()
        if (node.isObject) {
            val result = Transaction(user).use { txn ->
                deserializeRPCRequestAndExecute(txn, node, methods)
            }
            response.status = if (result.error == null) HttpServletResponse.SC_OK else HttpServletResponse.SC_BAD_REQUEST
            response.contentType = JsonGlobals.ContentTypeApplicationJson
            response.respond(JsonGlobals.JsonObjectMapper.writeValueAsString(result))

        } else {
            val results = Transaction(user).use { txn ->
                node.elements().asSequence()
                        .map { deserializeRPCRequestAndExecute(txn, it, methods) }
                        .toList()
            }
            response.respond(JsonGlobals.JsonObjectMapper.writeValueAsString(results))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        response.status = HttpServletResponse.SC_BAD_REQUEST
        response.respond(JsonGlobals.JsonObjectMapper.writeValueAsString(RPCResponse(
                id = 0,
                error = RPCError(
                        RPCError.CODE_PARSE_ERROR,
                        e.message ?: "Unknown parsing exception"
                )
        )))
    }
    baseRequest.isHandled = true
}


private fun deserializeRPCRequestAndExecute(transaction: Transaction, tree: JsonNode, methods: Map<String, RPCMethod>): RPCResponse {
    val id = try {
        tree.get("id").asInt()
    } catch (e: Exception) {
        return RPCResponse(
                id = 0,
                error = RPCError(
                        code = RPCError.CODE_INVALID_REQUEST,
                        message = "ID not provided."
                )
        )
    }

    val methodName = tree.get("method").asText()
    val method = methods[methodName] ?: return RPCResponse(
            id = id,
            error = RPCError(
                    code = RPCError.CODE_METHOD_NOT_FOUND,
                    message = "Method '$methodName' not found."
            )
    )

    val parameters = HashMap<String, Any?>(method.arguments.associate { it.key to it.default.value })
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
            try {
                parameters[key] = argument.type.parse(value)
            } catch (e: Exception) {
                return RPCResponse(
                        id = id,
                        error = RPCError(
                                code = RPCError.CODE_INVALID_PARAMS,
                                message = "Could not parse argument '${argument.key}' - expecting a ${argument.type.name}."
                        )
                )
            }
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
            try {
                parameters[argument.key] = argument.type.parse(value)
            } catch (e: Exception) {
                return RPCResponse(
                        id = id,
                        error = RPCError(
                                code = RPCError.CODE_INVALID_PARAMS,
                                message = "Could not parse argument '${argument.key}' - expecting a ${argument.type.name}."
                        )
                )
            }
        }
    }
    if (parameters.size != method.arguments.size) {
        val missing = method.arguments.asSequence()
                .map { it.key }
                .toSet()
                .subtract(parameters.keys)

        if (missing.isNotEmpty()) {
            return RPCResponse(
                    id = id,
                    error = RPCError(
                            code = RPCError.CODE_INVALID_PARAMS,
                            message = "Parameters ${missing.joinToString()} are missing"
                    )
            )
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