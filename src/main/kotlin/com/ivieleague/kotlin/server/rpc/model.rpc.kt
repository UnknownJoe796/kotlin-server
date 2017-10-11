package com.ivieleague.kotlin.server.rpc

import com.ivieleague.kotlin.server.model.Schema
import com.ivieleague.kotlin.server.model.TableAccess

fun TableAccess.registerRpc(schema: Schema, map: MutableMap<String, RPCMethod>) {
    map[table.tableName + ".get"] = RPCModelGet(this, schema)
    map[table.tableName + ".gets"] = RPCModelGets(this, schema)
    map[table.tableName + ".query"] = RPCModelQuery(this, schema)
    map[table.tableName + ".update"] = RPCModelUpdate(this, schema)
    map[table.tableName + ".delete"] = RPCModelDelete(this, schema)
}

fun Schema.registerRpc(map: MutableMap<String, RPCMethod>) {
    for (tableAccess in tableToAccess.values) {
        tableAccess.registerRpc(this, map)
    }
}