package com.ivieleague.kotlin.server

import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.content.TextContent
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.pipeline.PipelineContext
import org.jetbrains.ktor.pipeline.PipelineInterceptor
import org.jetbrains.ktor.routing.*

class HttpStatusCodeException(message: String?, val code: HttpStatusCode) : Exception(message)

fun exceptionNotFound(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.NotFound)

fun exceptionForbidden(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.Forbidden)

fun exceptionPayment(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.PaymentRequired)

fun exceptionUnauthorized(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.Unauthorized)

fun exceptionNotAllowed(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.MethodNotAllowed)

fun exceptionBadRequest(message: String?)
        = HttpStatusCodeException(message, HttpStatusCode.BadRequest)

suspend fun PipelineContext<ApplicationCall>.exceptionWrap(body: suspend PipelineContext<ApplicationCall>.(ApplicationCall) -> Unit) {
    try {
        body.invoke(this, this.subject)
    } catch(e: HttpStatusCodeException) {
        subject.respond(TextContent(e.message ?: "", ContentType.Text.Plain, e.code))
    } catch(e: UnsupportedOperationException) {
        subject.respond(TextContent(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.NotImplemented))
    }
}


/**
 * Builds a route to match `GET` requests with specified [path]
 */
fun Route.getException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = get(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `GET` requests
 */
fun Route.getException(body: PipelineInterceptor<ApplicationCall>): Route = get { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `POST` requests with specified [path]
 */
fun Route.postException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = post(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `POST` requests
 */
fun Route.postException(body: PipelineInterceptor<ApplicationCall>): Route = post { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `HEAD` requests with specified [path]
 */
fun Route.headException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = head(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `HEAD` requests
 */
fun Route.headException(body: PipelineInterceptor<ApplicationCall>): Route = head { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `PUT` requests with specified [path]
 */
fun Route.putException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = put(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `PUT` requests
 */
fun Route.putException(body: PipelineInterceptor<ApplicationCall>): Route = put { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `DELETE` requests with specified [path]
 */
fun Route.deleteException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = delete(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `DELETE` requests
 */
fun Route.deleteException(body: PipelineInterceptor<ApplicationCall>): Route = delete { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `OPTIONS` requests with specified [path]
 */
fun Route.optionsException(path: String, body: PipelineInterceptor<ApplicationCall>): Route = options(path) { exceptionWrap { body.invoke(this, it) } }

/**
 * Builds a route to match `OPTIONS` requests
 */
fun Route.optionsException(body: PipelineInterceptor<ApplicationCall>): Route = options { exceptionWrap { body.invoke(this, it) } }