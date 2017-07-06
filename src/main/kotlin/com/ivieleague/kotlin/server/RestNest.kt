package com.ivieleague.kotlin.server

import com.ivieleague.kotlin.server.model.*
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.*

fun Route.restNest(tableAccesses: Collection<TableAccess>, userGetter: (ApplicationCall) -> Instance? = { null }) {
    val tableMap = tableAccesses.associate { it.table.tableName to it.table }
    val tableToAccessMap = tableAccesses.associateBy { it.table }
    post("multi") {
        exceptionWrap {
            val user = userGetter.invoke(it)
            val requestString = it.request.receive<String>()
            val results = HashMap<String, Any?>()
            val result = try {
                val raw = JsonObjectMapper.readValue(requestString, LinkedHashMap::class.java) as LinkedHashMap<String, Any?>
                for ((key, rawRequest) in raw) {
                    val mapRequest = rawRequest as LinkedHashMap<String, Any?>
                    mapRequest.populate(results)
                    val request = mapRequest.toRequest(tableMap)
                    results[key] = request.invoke(user, tableToAccessMap[request.table]!!)
                }
                results
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            it.respondText(JsonObjectMapper.writeValueAsString(result))
        }
    }
    for (table in tableAccesses) {
        route(table.table.tableName) {
            restNest(table, userGetter)
        }
    }
}

fun MutableMap<String, Any?>.populate(data: Map<String, Any?>) {
    val changes = ArrayList<Pair<String, Any?>>()
    for ((key, value) in this) {
        when (value) {
            is MutableMap<*, *> -> (value as MutableMap<String, Any?>).populate(data)
            is String -> if (value.startsWith("\${") && value.length > 1 && value.endsWith("}")) {
                val path = value.substring(1).split('.')
                var current: Any? = data
                for (pathKey in path) {
                    current = when (current) {
                        is Map<*, *> -> current[pathKey]
                        is Instance -> {
                            val property = current.table.properties[pathKey]
                            when (property) {
                                is Scalar -> current.scalars[property]
                                is Link -> current.links[property]
                                is Multilink -> current.multilinks[property]
                                else -> throw IllegalArgumentException()
                            }
                        }
                        is List<*> -> current.getOrNull(pathKey.toIntOrNull() ?: throw IllegalArgumentException("Need index instead of argument"))
                        else -> throw IllegalArgumentException()
                    }
                }
                changes += key to current
            }
        }
    }
    this += changes
}

fun Route.restNest(tableAccess: TableAccess, userGetter: (ApplicationCall) -> Instance? = { null }) {
    options("") {
        exceptionWrap {
            val output = tableAccess.table.properties
            it.respondText(output.toString())//TODO
        }
    }
    get("") {
        exceptionWrap {
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, tableAccess.table.defaultRead())
            val stringResult = JsonObjectMapper.writeValueAsString(result)
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
            val stringResult = result?.let { JsonObjectMapper.writeValueAsString(it) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    post("/query") {
        exceptionWrap {
            val requestString = it.request.receive<String>()
            val request = try {
                (JsonObjectMapper.readValue(requestString, Map::class.java) as Map<String, Any?>).toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.query(userGetter.invoke(it), Condition.Always, request)
            val stringResult = JsonObjectMapper.writeValueAsString(result)
            it.respondText(stringResult, ContentType.Application.Json)
        }
    }
    post("/{id}/query") {
        exceptionWrap {
            val requestString = it.request.receive<String>()
            val request = try {
                (JsonObjectMapper.readValue(requestString, Map::class.java) as Map<String, Any?>).toRead(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.get(userGetter.invoke(it), call.parameters["id"]!!, request)
            val stringResult = result?.let { JsonObjectMapper.writeValueAsString(it) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    post("") {
        exceptionWrap {
            val inputString = it.request.receive<String>()
            val input = try {
                (JsonObjectMapper.readValue(inputString, Map::class.java) as Map<String, Any?>).toWrite(tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = tableAccess.update(userGetter.invoke(it), input)
            val stringResult = result.let { JsonObjectMapper.writeValueAsString(it) }
            it.respondText(stringResult ?: "{}", ContentType.Application.Json)
        }
    }
    put("/{id}") {
        exceptionWrap {
            val inputString = it.request.receive<String>()
            val input = try {
                (JsonObjectMapper.readValue(inputString, Map::class.java) as Map<String, Any?>).toWrite(call.parameters["id"]!!, tableAccess.table)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = try {
                tableAccess.update(userGetter.invoke(it), input)
            } catch(e: IllegalArgumentException) {
                throw exceptionNotFound(e.message)
            }
            val stringResult = result.let { JsonObjectMapper.writeValueAsString(it) }
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