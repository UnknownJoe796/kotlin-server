package com.ivieleague.kotlin.server.handler

import com.ivieleague.kotlin.server.respond
import com.lightningkite.kotlin.exception.stackTraceString
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


fun handler(
        onHandle: (
                target: String,
                baseRequest: Request,
                request: HttpServletRequest,
                response: HttpServletResponse
        ) -> Unit
) = object : AbstractHandler() {
    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        try {
            onHandle.invoke(target, baseRequest, request, response)
        } catch (e: HttpStatusCodeException) {
            if (!baseRequest.isHandled) {
                response.status = e.code
                response.respond(e.message ?: "")
                baseRequest.isHandled = true
            }
        } catch (e: Exception) {
            if (!baseRequest.isHandled) {
                response.status = 500
                response.respond(e.stackTraceString())
                baseRequest.isHandled = true
            }
        }
    }
}

fun handler(
        pattern: Regex,
        onHandle: (
                match: MatchResult,
                baseRequest: Request,
                request: HttpServletRequest,
                response: HttpServletResponse
        ) -> Unit
) = object : AbstractHandler() {
    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val match = pattern.matchEntire(target)
        if (match != null) {
            try {
                onHandle.invoke(match, baseRequest, request, response)
            } catch (e: HttpStatusCodeException) {
                if (!baseRequest.isHandled) {
                    response.status = e.code
                    response.respond(e.message ?: "")
                    baseRequest.isHandled = true
                }
            } catch (e: Exception) {
                if (!baseRequest.isHandled) {
                    response.status = 500
                    response.respond(e.stackTraceString())
                    baseRequest.isHandled = true
                }
            }
        }
    }
}
