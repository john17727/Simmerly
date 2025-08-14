package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine,
    sessionStorage: SessionStorage,
) = HttpClient(engine) {
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
    }
    install(Auth) {
        bearer {
            loadTokens {
                val latestToken = sessionStorage.getToken() ?: ""
                BearerTokens(latestToken, latestToken)
            }
            refreshTokens {
                val latestToken = sessionStorage.getToken() ?: ""
                val serverAddress = sessionStorage.getServerAddress() ?: ""
                val sessionClient = SessionClient(client, serverAddress)
                val newToken =
                    when (val result = sessionClient.refreshToken(latestToken)) {
                        is Result.Error -> ""
                        is Result.Success -> result.data
                    }
                sessionStorage.setToken(newToken)
                BearerTokens(newToken, newToken)
            }
        }
    }
}

expect fun getPlatformEngine(): HttpClientEngine

fun HttpRequestBuilder.withApiUrl(baseUrl: String, path: String) {

    url {
        takeFrom(Url(baseUrl)) // Use Ktor's Url to parse the base
        encodedPath = this.encodedPath + path // Append the specific endpoint path
    }
}