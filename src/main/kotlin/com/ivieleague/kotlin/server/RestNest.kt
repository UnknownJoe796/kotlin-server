package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.*
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.routing.*

fun Route.restNest(schema: Schema, userGetter: (ApplicationCall) -> Instance? = { null }) {
    post("multi") {
        exceptionWrap {
            val user = userGetter.invoke(it)
            val results = HashMap<String, Any?>()
            val result = try {
                val raw = it.request.receiveJson<LinkedHashMap<String, Any?>>()!!
                Transaction(userGetter.invoke(it)).use { txn ->
                    raw.entries.asSequence()
                            .map { it.key to (it.value as LinkedHashMap<String, Any?>).toRequest(schema.nameToTable) }
                            .forEach {
                                it.second.populate(results)
                                results[it.first] = it.second.invoke(txn, schema[it.second.table])
                            }
                }
                results
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            it.respondJson(result)
        }
    }
    options("multi") {
        exceptionWrap {
            it.respondJson(schema.nameToTable.mapValues { entry -> entry.value.toInfoMap(userGetter.invoke(it)) })
        }
    }
    for (access in schema.accesses) {
        route(access.table.tableName) {
            restNest(access, userGetter)
        }
    }
}

fun Route.restNest(tableAccess: TableAccess, userGetter: (ApplicationCall) -> Instance? = { null }) {
    options("") {
        exceptionWrap {
            it.respondJson(tableAccess.table.toInfoMap(userGetter.invoke(it)))
        }
    }
    get("") {
        exceptionWrap {
            Transaction(userGetter.invoke(it), readOnly = true).use { txn ->
                val result = tableAccess.query(txn, Condition.Always, tableAccess.table.defaultRead())
                it.respondJson(result)
            }
        }
    }
    get("/{id}") {
        exceptionWrap {
            Transaction(userGetter.invoke(it), readOnly = true).use { txn ->
                val result = tableAccess.get(txn, call.parameters["id"]!!, tableAccess.table.defaultRead())
                it.respondJson(result)
            }
        }
    }
    post("/query") {
        exceptionWrap {
            val request = try {
                it.request.receiveJson<Map<String, Any?>>()!!.toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            Transaction(userGetter.invoke(it), readOnly = true).use { txn ->
                val result = tableAccess.query(txn, Condition.Always, request)
                it.respondJson(result)
            }
        }
    }
    post("/{id}/query") {
        exceptionWrap {
            val request = try {
                it.request.receiveJson<Map<String, Any?>>()!!.toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            Transaction(userGetter.invoke(it), readOnly = true).use { txn ->
                val result = tableAccess.get(txn, call.parameters["id"]!!, request)
                it.respondJson(result)
            }
        }
    }
    post("") {
        exceptionWrap {
            val input = try {
                it.request.receiveJson<Map<String, Any?>>()!!.toWrite(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            Transaction(userGetter.invoke(it)).use { txn ->
                val result = tableAccess.update(txn, input)
                it.respondJson(result)
            }
        }
    }
    put("/{id}") {
        exceptionWrap {
            val input = try {
                it.request.receiveJson<Map<String, Any?>>()!!.toWrite(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            Transaction(userGetter.invoke(it)).use { txn ->
                val result = tableAccess.update(txn, input)
                it.respondJson(result)
            }
        }
    }
    delete("/{id}") {
        exceptionWrap {
            Transaction(userGetter.invoke(it)).use { txn ->
                val result = tableAccess.delete(txn, call.parameters["id"]!!)
                if (result) {
                    it.respondJson(result)
                } else {
                    throw exceptionNotFound("Item with id ${call.parameters["id"]!!} was not found.")
                }
            }
        }
    }
}