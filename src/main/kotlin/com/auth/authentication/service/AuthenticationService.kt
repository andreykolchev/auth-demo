package com.auth.authentication.service

import com.auth.authentication.controller.LoginRequest
import com.auth.authentication.controller.LoginResponse
import com.auth.authentication.controller.RegistrationRequest
import com.auth.exception.ErrorException
import com.auth.jwt.JwtManager
import com.auth.user.entity.User
import com.auth.user.repository.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.envers.AuditReaderFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationService(
    private val jwtManager: JwtManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    @PersistenceContext
    private val entityManager: EntityManager
) {

    /**
     * Registers a user based on the provided registration request.
     *
     * @param request the registration request containing user information
     * @throws ErrorException if the username already exists
     */
    fun register(request: RegistrationRequest) {
        if (userRepository.findByUsername(request.username) != null) {
            throw ErrorException("Username already exists.")
        }
        userRepository.save(
            User(
                username = request.username,
                password = passwordEncoder.encode(request.password)
            )
        )
    }

    /**
     * Performs user login based on the provided login request.
     *
     * @param request the login request containing username and password
     * @return the login response containing the generated token
     * @throws ErrorException if the user is not found with the given username
     */
    fun login(request: LoginRequest): LoginResponse {
        SecurityContextHolder.getContext().authentication =
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(request.username, request.password))

        val user = userRepository.findByUsername(request.username)
            ?: throw ErrorException("User not found with username: $request.username")
        user.incrementLoginCount()
        userRepository.save(user)

        val token = jwtManager.generateToken(user)
        return LoginResponse(token)
    }

    /**
     * Logs out the currently authenticated user by clearing the security context.
     */
    fun logout() {
        SecurityContextHolder.getContext().authentication = null
    }

    /**
     * Retrieves the last login dates of the currently authenticated user.
     *
     * @return a list of the last login dates, with a maximum of 5 dates
     */
    fun getLastLogins(): List<Date> {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val auditReader = AuditReaderFactory.get(entityManager)
        return auditReader
            .getRevisions(User::class.java, user.id)
            .takeLast(5)
            .map { revision -> auditReader.getRevisionDate(revision) }
    }
}