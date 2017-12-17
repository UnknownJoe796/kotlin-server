package com.ivieleague.kotlin.server.singleton

import com.ivieleague.kotlin.server.rpc.RPCMethod

/**
 * Generate RPC Calls
 * Created by josep on 11/18/2017.
 */
fun <T> SingletonAccess<T>.rpc(): Map<String, RPCMethod> {
    val map = HashMap<String, RPCMethod>()
    map["get"] = RPCSingletonGet(this)
    map["set"] = RPCSingletonSet(this)
    if (this is SingletonModifyAccess<*, *>) {
        map["set"] = RPCSingletonModify(this)
    }
    return map
}