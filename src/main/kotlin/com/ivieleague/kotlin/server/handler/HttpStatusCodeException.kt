package com.ivieleague.kotlin.server.handler

import javax.servlet.http.HttpServletResponse

class HttpStatusCodeException(message: String?, val code: Int) : Exception(message)

fun exceptionNotFound(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_NOT_FOUND)

fun exceptionForbidden(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_FORBIDDEN)

fun exceptionPayment(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_PAYMENT_REQUIRED)

fun exceptionUnauthorized(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_UNAUTHORIZED)

fun exceptionNotAllowed(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_METHOD_NOT_ALLOWED)

fun exceptionBadRequest(message: String?)
        = HttpStatusCodeException(message, HttpServletResponse.SC_BAD_REQUEST)