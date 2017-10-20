package com.ivieleague.kotlin.server.rpc

class RPCException(
        val rpcError: RPCError
) : Exception(rpcError.message)