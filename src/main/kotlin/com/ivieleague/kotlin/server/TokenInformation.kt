package com.ivieleague.kotlin.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.util.*

/**
 * An object that creates and validates JWTs.  It must be supplied with signing information through the [algorithm].
 * @param issuer The issuer of the token.
 * @param expireMilliseconds The number of milliseconds the token should remain active for.
 * @param algorithm The algorithm used to sign tokens.
 */
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
            .withIssuedAt(Date())
            .sign(algorithm)

    fun getUserId(token: String): String {
        val id = try {
            verifier
                    .verify(token)
                    .getClaim("user_id")
                    .asString() ?: throw IllegalArgumentException("JWT does not contain a user_id claim!")
        } catch (e: TokenExpiredException) {
            throw IllegalArgumentException("This token has expired")
        } catch (e: JWTVerificationException) {
            throw IllegalArgumentException("The token could not be parsed properly")
        }
        return id
    }

    data class TokenInfo(
            var userId: String,
            var dateIssued: Date?
    )

    fun getInfo(token: String): TokenInfo {
        try {
            val decoded = verifier.verify(token)

            val id = decoded
                    .getClaim("user_id")
                    .asString() ?: throw IllegalArgumentException("JWT does not contain a user_id claim!")

            val issued = decoded.issuedAt

            return TokenInfo(
                    userId = id,
                    dateIssued = issued
            )

        } catch(e: TokenExpiredException) {
            throw IllegalArgumentException("This token has expired")
        } catch(e: JWTVerificationException) {
            throw IllegalArgumentException("The token could not be parsed properly")
        }
    }
}