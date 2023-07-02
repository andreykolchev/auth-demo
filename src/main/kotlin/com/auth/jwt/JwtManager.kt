package com.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtManager(

    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration}")
    private val expiration: Long
) {

    /**
     * Generates a JWT token for the provided user details.
     *
     * @param userDetails the user details used to generate the token
     * @return the generated JWT token
     */
    fun generateToken(userDetails: UserDetails): String {
        val claims: Claims = Jwts.claims().setSubject(userDetails.username)

        val issuedAt = Date()
        val expiration = Date(issuedAt.time + expiration)

        return Jwts.builder().setClaims(claims).setSubject(userDetails.username)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact()
    }

    /**
     * Extracts the token from an Authorization header string.
     *
     * @param authHeader the Authorization header string
     * @return the extracted token string
     */
    fun extractToken(authHeader: String): String {
        return authHeader.substring(7)
    }

    /**
     * Validates a JWT token for the provided token and user details.
     *
     * @param token the JWT token to validate
     * @param userDetails the user details used for validation
     * @return true if the token is valid, false otherwise
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return extractUsername(token) == userDetails.username && !isTokenExpired(token)
    }

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the extracted username
     */
    fun extractUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey()).build()
            .parseClaimsJws(token)
            .body.subject
    }

    private fun isTokenExpired(token: String): Boolean {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey()).build()
            .parseClaimsJws(token)
            .body
            .expiration
            .before(Date())
    }

    private fun getSigningKey(): Key? {
        val keyBytes = Decoders.BASE64.decode(secret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}