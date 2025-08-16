package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.core.data.network.utils.DynamicBaseUrl
import dev.juanrincon.simmerly.core.domain.network.BaseUrlProvider
import dev.juanrincon.simmerly.core.domain.network.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createAuthenticatedHttpClient(
    engine: HttpClientEngine,
    baseUrlProvider: BaseUrlProvider,
    tokenProvider: TokenProvider
) = HttpClient(engine) {
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 5000
    }
    install(Logging) {
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
        contentType(ContentType.Application.Json)
        url("https://fallback.com") // Will be replaced
    }
    install(Auth) {
        bearer {
            loadTokens {
                val latestToken = tokenProvider.getToken() ?: ""
                BearerTokens(latestToken, latestToken)
            }
            refreshTokens {
                val latestToken = tokenProvider.getToken() ?: ""
                val serverAddress = baseUrlProvider.current() ?: ""
                val sessionClient = SessionClient(client, serverAddress)
                val newToken =
                    when (val result = sessionClient.refreshToken(latestToken)) {
                        is Result.Error -> ""
                        is Result.Success -> result.data
                    }
                tokenProvider.setToken(newToken)
                BearerTokens(newToken, newToken)
            }
        }
    }
    install(DynamicBaseUrl) {
        dynamicBaseUrlProvider = {
            baseUrlProvider.current()
        }
    }
}