package com.ivieleague.kotlin.server.access

import com.ivieleague.kotlin.server.rpc.RPCMethod

/**
 * Generate RPC Calls
 * Created by josep on 11/18/2017.
 */
fun <T> DAO<T>.rpc(): Map<String, RPCMethod> {
    val map = HashMap<String, RPCMethod>()
    map["create"] = RPCCreate(this)
    map["delete"] = RPCDelete(this)
    map["get"] = RPCGet(this)
    map["set"] = RPCSet(this)
    if (this is ModifyDAO<*, *>) {
        map["modify"] = RPCModify(this)
    }
    if (this is QueryableDAO<*>) {
        map["query"] = RPCQuery(this)
    }
    return map
}