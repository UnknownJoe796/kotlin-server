package com.ivieleague.kotlin.server.auth

import com.auth0.jwt.algorithms.Algorithm
import com.ivieleague.kotlin.server.exceptionBadRequest
import com.ivieleague.kotlin.server.exceptionForbidden
import com.ivieleague.kotlin.server.exceptionWrap
import com.ivieleague.kotlin.server.json
import com.ivieleague.kotlin.server.model.Table
import com.ivieleague.kotlin.server.model.TableAccess
import org.jetbrains.ktor.application.receive
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.post
import java.util.*

fun <T : Table> Routing.makeTokenEndpoint(
        path: String,
        tableAccess: TableAccess,
        issuer: String,
        validTime: Long,
        algorithm: Algorithm
): Route {
    assert(tableAccess.table is AuthUser<*>)
    return post(path) {
        exceptionWrap {
            val inputString = it.request.receive<String>()
            val input = try {
                json.readValue(inputString, Map::class.java)
            } catch(e: Exception) {
                throw exceptionBadRequest(e.message)
            }
            val username = input["username"] as? String ?: throw exceptionBadRequest("'username' not provided")
            val password = input["password"] as? String ?: throw exceptionBadRequest("'password' not provided")
            val token = AuthUser.token<T>(
                    tableAccess = tableAccess,
                    issuer = issuer,
                    expire = Date(System.currentTimeMillis() + validTime),
                    algorithm = algorithm,
                    username = username,
                    password = password
            ) ?: throw exceptionForbidden("Username and password do not match")
            it.respondText(token, ContentType.Application.Any)
        }
    }
}
