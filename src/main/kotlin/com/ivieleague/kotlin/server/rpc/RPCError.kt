package com.ivieleague.kotlin.server.rpc

data class RPCError(
        val code: Int,
        val message: String,
        val data: Any? = null
) {
    companion object {
        const val CODE_PARSE_ERROR = -32700
        const val CODE_INVALID_REQUEST = -32600
        const val CODE_METHOD_NOT_FOUND = -32601
        const val CODE_INVALID_PARAMS = -32602
        const val CODE_INTERNAL_ERROR = -32603
    }
}