package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.Condition
import com.ivieleague.kotlin.server.model.Instance
import com.ivieleague.kotlin.server.model.TableAccess
import com.ivieleague.kotlin.server.model.populate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.routing.*

fun Route.restNest(tableAccesses: Collection<TableAccess>, userGetter: (ApplicationCall) -> Instance? = { null }) {
    val tableMap = tableAccesses.associate { it.table.tableName to it.table }
    val tableToAccessMap = tableAccesses.associateBy { it.table }
    post("multi") {
        exceptionWrap {
            val user = userGetter.invoke(it)
            val results = HashMap<String, Any?>()
            val result = try {
                val raw = it.request.receiveJson<LinkedHashMap<String, Any?>>()!!
                raw.entries.asSequence()
                        .map { it.key to (it.value as LinkedHashMap<String, Any?>).toRequest(tableMap) }
                        .forEach {
                            it.second.populate(results)
                            results[it.first] = it.second.invoke(user, tableToAccessMap[it.second.table]!!)
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
            it.respondJson(tableMap.mapValues { entry -> entry.value.toInfoMap(userGetter.invoke(it)) })
        }
    }
    for (table in tableAccesses) {
        route(table.table.tableName) {
            restNest(table, userGetter)
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
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, tableAccess.table.defaultRead())
            it.respondJson(result)
        }
    }
    get("/{id}") {
        exceptionWrap {
            val result = try {
                tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, tableAccess.table.defaultRead())
            } catch(e: Exception) {
                throw exceptionNotFound(e.message)
            }
            it.respondJson(result)
        }
    }
    post("/query") {
        exceptionWrap {
            val request = try {
                it.request.receive<Map<String, Any?>>().toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, request)
            it.respondJson(result)
        }
    }
    post("/{id}/query") {
        exceptionWrap {
            val request = try {
                it.request.receive<Map<String, Any?>>().toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, request)
            it.respondJson(result)
        }
    }
    post("") {
        exceptionWrap {
            val input = try {
                it.request.receive<Map<String, Any?>>().toWrite(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.update(userGetter.invoke(it), input)
            it.respondJson(result)
        }
    }
    put("/{id}") {
        exceptionWrap {
            val input = try {
                it.request.receive<Map<String, Any?>>().toWrite(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = try {
                tableAccess.update(userGetter.invoke(it), input)
            } catch(e: IllegalArgumentException) {
                throw exceptionNotFound(e.message)
            }
            it.respondJson(result)
        }
    }
    delete("/{id}") {
        exceptionWrap {
            val result = tableAccess.delete(userGetter.invoke(it), call.parameters["id"]!!)
            if (result) {
                it.respondJson(result)
            } else {
                throw exceptionNotFound("Item with id ${call.parameters["id"]!!} was not found.")
            }
        }
    }
}