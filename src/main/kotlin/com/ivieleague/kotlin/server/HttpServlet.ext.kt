package com.ivieleague.kotlin.server

import com.fasterxml.jackson.databind.JsonNode
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
        onHandle.invoke(target, baseRequest, request, response)
    }
}

fun HttpServletRequest.getContent(): String {
    return this.reader.readText()
}

fun HttpServletResponse.respond(value: String) {
    this.writer.apply {
        write(value)
        flush()
        close()
    }

}

fun HttpServletRequest.getContentJson(): JsonNode {
    return JsonGlobals.JsonObjectMapper.readTree(reader)
}

fun HttpServletResponse.respond(value: JsonNode) {
    this.writer.apply {
        JsonGlobals.JsonObjectMapper.writeTree(
                JsonGlobals.jsonFactory.createGenerator(this),
                value
        )
        flush()
        close()
    }
}