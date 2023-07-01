package com.auth.jwt

import com.auth.user.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.http.HttpServletResponse as HttpServletResponse1

@Component
class JwtFilter(
    private val jwtManager: JwtManager,
    private val userDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse1,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader(AUTHORIZATION)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.substring(7)
            val username = jwtManager.extractUsername(token)
            when (SecurityContextHolder.getContext().authentication) {
                null -> {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    if (jwtManager.validateToken(token, userDetails)) {
                        SecurityContextHolder.getContext().authentication =
                            UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    }
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}