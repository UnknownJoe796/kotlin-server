package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.exceptionBadRequest
import com.ivieleague.kotlin.server.exceptionWrap
import com.ivieleague.kotlin.server.model.Schema
import com.ivieleague.kotlin.server.receiveJson
import com.ivieleague.kotlin.server.respondJson
import com.ivieleague.kotlin.server.type.Primitive
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.post

fun Route.restLogin(schema: Schema, userTableAccess: UserTableAccess, usernamePrimitive: Primitive) {
    post {
        exceptionWrap {
            val (email, password) = try {
                val request = it.request.receiveJson<Map<String, Any?>>()!!
                (request[usernamePrimitive.key] as String) to (request["password"] as String)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = userTableAccess.login(schema, usernamePrimitive, email, password)
            it.respondJson(result)
        }
    }
}