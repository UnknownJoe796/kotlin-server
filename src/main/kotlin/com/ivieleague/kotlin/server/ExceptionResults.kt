package com.ivieleague.kotlin.server

import io.ktor.application.ApplicationCall
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond

/**
 * An exception which, instead of resulting in a 500 error for the user, results in something else.
 */
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

suspend fun PipelineContext<Unit, ApplicationCall>.exceptionWrap(
        body: suspend PipelineContext<Unit, ApplicationCall>.(ApplicationCall) -> Unit
) {
    try {
        body.invoke(this, this.context)
    } catch(e: HttpStatusCodeException) {
        context.respond(TextContent(e.message ?: "", ContentType.Text.Plain, e.code))
    } catch(e: UnsupportedOperationException) {
        context.respond(TextContent(e.message ?: "", ContentType.Text.Plain, HttpStatusCode.NotImplemented))
    }
}