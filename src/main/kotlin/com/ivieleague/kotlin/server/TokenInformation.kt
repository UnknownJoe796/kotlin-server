package com.ivieleague.kotlin.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.util.*

data class TokenInformation(
        val issuer: String,
        val expireMilliseconds: Long,
        val algorithm: Algorithm
) {
    val verifier = JWT.require(algorithm)
            .acceptExpiresAt(0)
            .withIssuer(issuer)
            .build()!!

    fun token(userId: String) = JWT.create()
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expireMilliseconds))
            .withClaim("user_id", userId)
            .sign(algorithm)

    fun getUserId(token: String): String {
        val id = try {
            verifier
                    .verify(token)
                    .getClaim("user_id")
                    .asString() ?: throw IllegalArgumentException("JWT does not contain a user_id claim!")
        } catch(e: TokenExpiredException) {
            throw exceptionUnauthorized("This token has expired")
        } catch(e: JWTVerificationException) {
            throw exceptionBadRequest("The token could not be parsed properly")
        }
        return id
    }
}