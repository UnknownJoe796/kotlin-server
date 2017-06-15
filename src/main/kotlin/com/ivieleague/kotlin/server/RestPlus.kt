package com.ivieleague.kotlin.server

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.*

val json = ObjectMapper()

fun Route.restPlus(dao: DAO, schema: Schema) {
    for ((_, table) in schema.tables) {
        restPlus(dao, table)
    }
}

fun Route.restPlus(dao: DAO, table: Table) {
    get("${table.tableName}") {
        val result = dao.query(table, Condition.Always, table.defaultOutput())
        val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
        it.respondText(stringResult, ContentType.Application.Json)
    }
    get("${table.tableName}/{id}") {
        val result = dao.get(table, call.parameters["id"]!!, table.defaultOutput())
        val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    post("${table.tableName}/query") {
        val requestString = it.request.receive<String>()
        val request = JSON.parseOutput(table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
        val result = dao.query(table, Condition.Always, request)
        val stringResult = json.writeValueAsString(result.map { JSON.serializeInstance(it) })
        it.respondText(stringResult, ContentType.Application.Json)
    }
    post("${table.tableName}/{id}/query") {
        val requestString = it.request.receive<String>()
        val request = JSON.parseOutput(table, json.readValue(requestString, Map::class.java) as Map<String, Any?>)
        val result = dao.get(table, call.parameters["id"]!!, request)
        val stringResult = result?.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    post("${table.tableName}") {
        val inputString = it.request.receive<String>()
        val input = JSON.parseInput(table, null, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
        val result = dao.update(table, input)
        val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    put("${table.tableName}/{id}") {
        val inputString = it.request.receive<String>()
        val input = JSON.parseInput(table, call.parameters["id"]!!, json.readValue(inputString, Map::class.java) as Map<String, Any?>)
        val result = dao.update(table, input)
        val stringResult = result.let { json.writeValueAsString(JSON.serializeInstance(it)) }
        it.respondText(stringResult ?: "{}", ContentType.Application.Json)
    }
    delete("${table.tableName}/{id}") {
        val result = dao.delete(table, call.parameters["id"]!!)
        if (result) {
            it.respondText("$result", ContentType.Application.Json)
        } else {
            it.respondText("$result", ContentType.Application.Json)
        }
    }
}