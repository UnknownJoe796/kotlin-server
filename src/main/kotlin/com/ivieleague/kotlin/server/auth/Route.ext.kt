package com.ivieleague.kotlin.server.auth

import com.ivieleague.kotlin.server.exceptionBadRequest
import com.ivieleague.kotlin.server.exceptionWrap
import com.ivieleague.kotlin.server.model.Scalar
import com.ivieleague.kotlin.server.receiveJson
import com.ivieleague.kotlin.server.respondJson
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.post

fun Route.restLogin(userTableAccess: UserTableAccess, usernameScalar: Scalar) {
    post {
        exceptionWrap {
            val (email, password) = try {
                val request = it.request.receiveJson<Map<String, Any?>>()!!
                (request[usernameScalar.key] as String) to (request["password"] as String)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val result = userTableAccess.login(usernameScalar, email, password)
            it.respondJson(result)
        }
    }
}