package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin.DynamicBaseUrl
import dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin.supplier
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
    sessionDatastore: SessionDataStore
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
                val latestToken = sessionDatastore.getToken() ?: ""
                BearerTokens(latestToken, latestToken)
            }
            refreshTokens {
                val latestToken = sessionDatastore.getToken() ?: ""
                val serverAddress = sessionDatastore.getServerAddress() ?: ""
                val sessionClient = SessionClient(client, serverAddress)
                val newToken =
                    when (val result = sessionClient.refreshToken(latestToken)) {
                        is Result.Error -> ""
                        is Result.Success -> result.data
                    }
                sessionDatastore.setToken(newToken)
                BearerTokens(newToken, newToken)
            }
        }
    }
    install(DynamicBaseUrl) {
        supplier {
            loadBaseUrl {
                sessionDatastore.getServerAddress()
            }
        }
    }
}