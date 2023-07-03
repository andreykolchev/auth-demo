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


@Aspect
@Component
class AllowedAuthoritiesAspect {

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