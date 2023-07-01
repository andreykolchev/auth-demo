package com.auth

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


@Testcontainers
abstract class AbstractIntegrationTest {

    companion object {
        private const val COMPATIBLE_IMAGE_NAME = "postgres"
        private const val FULL_IMAGE_NAME = "postgres:12-alpine"
        private val POSTGRES_DB_CONTAINER: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>(
            DockerImageName.parse(FULL_IMAGE_NAME).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME)
        )
        private const val DATASOURCE_URL_PROPERTY_NAME = "spring.datasource.url"
        private const val DATASOURCE_PASSWORD_PROPERTY_NAME = "spring.datasource.password"
        private const val DATASOURCE_USERNAME_PROPERTY_NAME = "spring.datasource.username"

        init {
            POSTGRES_DB_CONTAINER.start()
        }

        @Suppress("unused")
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add(DATASOURCE_URL_PROPERTY_NAME) { POSTGRES_DB_CONTAINER.jdbcUrl }
            registry.add(DATASOURCE_PASSWORD_PROPERTY_NAME) { POSTGRES_DB_CONTAINER.password }
            registry.add(DATASOURCE_USERNAME_PROPERTY_NAME) { POSTGRES_DB_CONTAINER.username }
        }
    }
}