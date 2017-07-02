package com.ivieleague.kotlin.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.ivieleague.kotlin.server.model.Condition
import com.ivieleague.kotlin.server.model.Instance
import com.ivieleague.kotlin.server.model.TableAccess
import com.ivieleague.kotlin.server.model.defaultRead
import com.lightningkite.kotlin.networking.gsonToString
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.*

val json = ObjectMapper()

fun Route.restPlus(tableAccesses: Collection<TableAccess>, userGetter: (ApplicationCall) -> Instance? = { null }) {
    for (table in tableAccesses) {
        route(table.table.tableName) {
            restPlus(table, userGetter)
        }
    }
}

fun Route.restPlus(tableAccess: TableAccess, userGetter: (ApplicationCall) -> Instance? = { null }) {
    options("") {
        exceptionWrap {
            it.respondText(tableAccess.table.gsonToString())
        }
    }
    get("") {
        exceptionWrap {
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, tableAccess.table.defaultRead())
            val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
            it.respondText(stringResult, ContentType.Application.Json)
        }
    }
    get("/{id}") {
        exceptionWrap {
            val result = try {
                tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, tableAccess.table.defaultRead())
            } catch(e: Exception) {
                throw exceptionNotFound(e.message)
            }
            val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    post("/query") {
        exceptionWrap {
            val requestString = it.request.receive<String>()
            val request = try {
                JSON.parseRead(tableAccess.table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, request)
            val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
            it.respondText(stringResult, ContentType.Application.Json)
        }
    }
    post("/{id}/query") {
        exceptionWrap {
            val requestString = it.request.receive<String>()
            val request = try {
                JSON.parseRead(tableAccess.table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, request)
            val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    post("") {
        exceptionWrap {
            val inputString = it.request.receive<String>()
            val input = try {
                JSON.parseWrite(tableAccess.table, null, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.update(userGetter.invoke(it), input)
            val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    put("/{id}") {
        exceptionWrap {
            val inputString = it.request.receive<String>()
            val input = try {
                JSON.parseWrite(tableAccess.table, call.parameters["id"]!!, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = try {
                tableAccess.update(userGetter.invoke(it), input)
            } catch(e: IllegalArgumentException) {
                throw exceptionNotFound(e.message)
            }
            val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    delete("/{id}") {
        exceptionWrap {
            val result = tableAccess.delete(userGetter.invoke(it), call.parameters["id"]!!)
            if (result) {
                it.respondText("$result", ContentType.Application.Json)
            } else {
                throw exceptionNotFound("Item with id ${call.parameters["id"]!!} was not found.")
            }
        }
    }
}