package com.auth.authorization

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.nio.file.AccessDeniedException
import java.util.*

/**
 * Aspect for checking allowed authorities on annotated methods.
 */
@Aspect
@Component
class AllowedAuthoritiesAspect {

    /**
     * Checks the allowed authorities before executing the annotated method.
     *
     * @param pjp the ProceedingJoinPoint object
     * @return the result of the method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("@annotation(com.auth.authorization.AllowedAuthorities)")
    @Throws(Throwable::class)
    fun checkPermissions(pjp: ProceedingJoinPoint): Any {
        val method = (pjp.signature as MethodSignature).method
        val allowedAuthorities = method.getAnnotation(AllowedAuthorities::class.java)
        return if (allowedAuthorities.values.map { it.name }.containsAll(userAuthorities)){
            pjp.proceed()
        } else {
            throw AccessDeniedException("Access denied, needed permission:$allowedAuthorities.value.name")
        }
    }

    private val userAuthorities: Set<String>
        get() = AuthorityUtils.authorityListToSet(SecurityContextHolder.getContext().authentication.authorities)
}