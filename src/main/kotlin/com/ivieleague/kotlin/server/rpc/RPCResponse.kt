package com.ivieleague.kotlin.server.rpc

data class RPCResponse(
        val id: Int,
        val result: Any? = null,
        val error: RPCError? = null
)