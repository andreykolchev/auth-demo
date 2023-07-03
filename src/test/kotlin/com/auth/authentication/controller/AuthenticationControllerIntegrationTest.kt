package com.auth.authentication.controller

import com.auth.AbstractIntegrationTest
import com.auth.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
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
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AuthenticationControllerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    private val UNDER_TEST = "/api/v1/auth"

    private var objectMapper: ObjectMapper = jacksonObjectMapper()

    var token: String = ""

    @AfterAll
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Test
    @Order(1)
    fun shouldRegisterUser() {
        val request = RegistrationRequest("username", "password", "USER")

        mockMvc
            .perform(
                post("$UNDER_TEST/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn()
    }

    @Test
    @Order(2)
    fun shouldLoginUser() {
        val request = LoginRequest("username", "password")

        val res = mockMvc
            .perform(
                post("$UNDER_TEST/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        token = objectMapper.readValue<LoginResponse>(res.response.contentAsString).token

        assertNotNull(token)

    }

    @Test
    @Order(3)
    fun shouldGetLastLogins() {
        val res = mockMvc
            .perform(
                get("$UNDER_TEST/last-logins")
                    .header(AUTHORIZATION, "Bearer $token")
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val response: List<Date> = objectMapper.readValue<List<Date>>(res.response.contentAsString)

        assertTrue(response.isNotEmpty())
    }

    @Test
    @Order(4)
    fun shouldLogoutUser() {
        mockMvc
            .perform(
                get("$UNDER_TEST/logout")
                    .header(AUTHORIZATION, "Bearer $token")
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn()
    }

    @Test
    @Order(5)
    fun shouldGetError() {
        val request = RegistrationRequest("username", "password", "USER")

        val res = mockMvc
            .perform(
                post("$UNDER_TEST/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
            .andReturn()

        assertEquals("Username already exists.", res.response.contentAsString)
    }

    @Test
    @Order(6)
    fun shouldGetForbidden() {
        mockMvc
            .perform(
                get("$UNDER_TEST/last-logins")
            )
            .andExpect(status().isForbidden)
            .andReturn()
    }
}