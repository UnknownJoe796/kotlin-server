package com.ivieleague.kotlin.server.rpc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ivieleague.kotlin.server.bodyAsJson
import com.ivieleague.kotlin.server.responseMapper
import com.ivieleague.kotlin.server.type.SType
import com.ivieleague.kotlin.server.type.TypedObject
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

fun rpc(
        methods: Map<String, RPCMethod>,
        userGetter: (Request) -> TypedObject? = { null }
): (Request, Response) -> Any? = { request: Request, response: Response ->
    val responseMapper = request.responseMapper()
    val user = userGetter.invoke(request)
    try {
        val node = request.bodyAsJson()
        if (node.isObject) {
            val result = Transaction(user).use { txn ->
                deserializeRPCRequestAndExecute(txn, responseMapper, node, methods)
            }
            response.status(if (result.error == null) HttpServletResponse.SC_OK else HttpServletResponse.SC_BAD_REQUEST)
            result

        } else {
            val results = Transaction(user).use { txn ->
                node.elements().asSequence()
                        .map { deserializeRPCRequestAndExecute(txn, responseMapper, it, methods) }
                        .toList()
            }
            results
        }
    } catch (e: Exception) {
        e.printStackTrace()
        RPCResponse(
                id = 0,
                error = RPCError(
                        RPCError.CODE_PARSE_ERROR,
                        e.message ?: "Unknown parsing exception"
                )
        )
    }
}

fun RPCMethod.invokeJson(
        transaction: Transaction,
        arguments: JsonNode
) = invoke(transaction = transaction, arguments = arguments.let {
    val result = HashMap<String, Any?>()
    for ((key, value) in it.fields()) {
        val argument = this.arguments.find { it.key == key }
                ?: throw IllegalArgumentException("No parameter '$key' found.")
        result[key] = argument.type.parse(value)
    }
    result
})


private fun deserializeRPCRequestAndExecute(
        transaction: Transaction,
        mapper: ObjectMapper,
        tree: JsonNode,
        methods: Map<String, RPCMethod>
): RPCResponse {
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
                result = (method.returns.type as SType<Any?>).serialize(mapper.nodeFactory, result)
        )
    } catch (e: RPCException) {
        RPCResponse(
                id,
                error = e.rpcError
        )
    }
}