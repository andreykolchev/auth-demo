package com.auth.authentication.controller

import com.auth.AbstractIntegrationTest
import com.auth.authentication.service.AuthenticationService
import com.auth.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerIntegrationTest : AbstractIntegrationTest() {

    private val UNDER_TEST = "/api/v1/auth"

    @Autowired
    private lateinit var mockMvc: MockMvc

    private var objectMapper: ObjectMapper = jacksonObjectMapper()

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Test
    fun shouldRegisterUser() {
        val request = RegistrationRequest("username", "password")

        mockMvc
            .perform(
                post("$UNDER_TEST/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().is2xxSuccessful())
            .andReturn()
    }

    @Test
    fun shouldLoginUser() {
        authenticationService.register(RegistrationRequest(username = "username", password = "password"))
        val request = LoginRequest("username", "password")

        val res = mockMvc
            .perform(
                post("$UNDER_TEST/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().is2xxSuccessful())
            .andReturn()

        val response: LoginResponse = objectMapper.readValue<LoginResponse>(res.response.contentAsString)

        assertNotNull(response.token)
    }

    @Test
    fun shouldGetLastLogins() {
        authenticationService.register(RegistrationRequest(username = "username", password = "password"))
        val login = authenticationService.login(LoginRequest(username = "username", password = "password"))

        val res = mockMvc
            .perform(
                get("$UNDER_TEST/last-logins")
                    .header(AUTHORIZATION, "Bearer " + login.token)
            )
            .andExpect(status().is2xxSuccessful())
            .andReturn()

        val response: List<Date> = objectMapper.readValue<List<Date>>(res.response.contentAsString)

        assertTrue(response.isNotEmpty())
    }

    @Test
    fun shouldLogoutUser() {
        authenticationService.register(RegistrationRequest(username = "username", password = "password"))
        val login = authenticationService.login(LoginRequest(username = "username", password = "password"))

        mockMvc
            .perform(
                get("$UNDER_TEST/logout")
                    .header(AUTHORIZATION, "Bearer " + login.token)
            )
            .andExpect(status().is2xxSuccessful())
            .andReturn()
    }
}