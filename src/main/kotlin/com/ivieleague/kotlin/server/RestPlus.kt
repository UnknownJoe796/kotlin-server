package com.ivieleague.kotlin.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.ivieleague.kotlin.server.core.Condition
import com.ivieleague.kotlin.server.core.Instance
import com.ivieleague.kotlin.server.core.TableAccess
import com.ivieleague.kotlin.server.core.defaultRead
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.*

val json = ObjectMapper()

fun Route.restPlus(tableAccess: TableAccess, userGetter: (ApplicationCall) -> Instance? = { null }) {
    get("") {
        val result = tableAccess.query(userGetter.invoke(it), Condition.Always, tableAccess.table.defaultRead())
        val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
        it.respondText(stringResult, ContentType.Application.Json)
    }
    get("/{id}") {
        val result = tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, tableAccess.table.defaultRead())
        val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    post("/query") {
        val requestString = it.request.receive<String>()
        val request = JSON.parseRead(tableAccess.table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
        val result = tableAccess.query(userGetter.invoke(it), Condition.Always, request)
        val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
        it.respondText(stringResult, ContentType.Application.Json)
    }
    post("/{id}/query") {
        val requestString = it.request.receive<String>()
        val request = JSON.parseRead(tableAccess.table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
        val result = tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, tableAccess.table.defaultRead())
        val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    post("") {
        val inputString = it.request.receive<String>()
        val input = JSON.parseWrite(tableAccess.table, null, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
        val result = tableAccess.update(userGetter.invoke(it), input)
        val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    put("/{id}") {
        val inputString = it.request.receive<String>()
        val input = JSON.parseWrite(tableAccess.table, call.parameters["id"]!!, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
        val result = tableAccess.update(userGetter.invoke(it), input)
        val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    delete("/{id}") {
        val result = tableAccess.delete(userGetter.invoke(it), call.parameters["id"]!!)
        if (result) {
            it.respondText("$result", ContentType.Application.Json)
        } else {
            it.respondText("$result", ContentType.Application.Json)
        }
    }
}