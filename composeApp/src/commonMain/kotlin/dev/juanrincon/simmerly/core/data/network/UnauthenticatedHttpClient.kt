package dev.juanrincon.simmerly.core.data.network

import dev.juanrincon.simmerly.core.data.network.utils.DynamicBaseUrl
import dev.juanrincon.simmerly.core.domain.network.BaseUrlProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createUnauthenticatedHttpClient(
    engine: HttpClientEngine,
    baseUrlProvider: BaseUrlProvider
) = HttpClient(engine) {
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 5000
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
            }
        )
    }
    defaultRequest {
        url("https://fallback.com") // Will be replaced
    }
    install(DynamicBaseUrl) {
        dynamicBaseUrlProvider = {
            baseUrlProvider.current()
        }
    }
}