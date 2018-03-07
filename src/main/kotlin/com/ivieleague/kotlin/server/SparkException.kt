package com.ivieleague.kotlin.server

import javax.servlet.http.HttpServletResponse

/**
 * An exception which, instead of resulting in a 500 error for the user, results in something else.
 */
class HttpStatusCodeException(
        val item: Any?,
        val itemType: Class<*>,
        val code: Int,
        message: String? = item.toString()
) : Exception(message)

inline fun <reified T> HttpStatusCodeException(
        item: T?,
        code: Int,
        message: String? = item.toString()
): HttpStatusCodeException = HttpStatusCodeException(
        item = item,
        itemType = T::class.java,
        code = code,
        message = message
)

inline fun <reified T> exceptionNotFound(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_NOT_FOUND)

inline fun <reified T> exceptionForbidden(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_FORBIDDEN)

inline fun <reified T> exceptionPayment(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_PAYMENT_REQUIRED)

inline fun <reified T> exceptionUnauthorized(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_UNAUTHORIZED)

inline fun <reified T> exceptionNotAllowed(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_METHOD_NOT_ALLOWED)

inline fun <reified T> exceptionBadRequest(message: T?) = HttpStatusCodeException(message, HttpServletResponse.SC_BAD_REQUEST)