package com.auth.authentication.controller

import com.auth.authentication.service.AuthenticationService
import com.auth.authorization.AllowedAuthorities
import com.auth.authorization.Authority.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {

    @Operation(summary = "Registers user with the provided credentials (sign-up).")
    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegistrationRequest): ResponseEntity<String> {
        authenticationService.register(request)
        return ResponseEntity("User registered successfully", HttpStatus.OK)
    }

    @Operation(
        summary = "Login user (sign-in).",
        description = "Logs in a user with the provided credentials. Returns token value",
        responses = [ApiResponse(responseCode = "200", content = [Content(mediaType = "application/json")])]
    )
    @PostMapping("/login")
    fun loginUser(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authenticationService.login(request))
    }

    @Operation(summary = "Logout user (sign-out).")
    @GetMapping("/logout")
    fun logoutUser(): ResponseEntity<String> {
        authenticationService.logout()
        return ResponseEntity("User logged out successfully", HttpStatus.OK)
    }

    @Operation(
        summary = "Last logins history.",
        description = "Returns a list of dates (5 last user logins)",
        responses = [ApiResponse(responseCode = "200", content = [Content(mediaType = "application/json")])]
    )
    @GetMapping("/last-logins")
    @AllowedAuthorities(USER)
    fun getLastLogins(): ResponseEntity<List<Date>> {
        return ResponseEntity(authenticationService.getLastLogins(), HttpStatus.OK)
    }
}

data class RegistrationRequest(val username: String, val password: String, val authority: String)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)