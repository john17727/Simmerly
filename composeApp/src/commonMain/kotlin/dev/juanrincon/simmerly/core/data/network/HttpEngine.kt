package dev.juanrincon.simmerly.core.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom

expect fun getPlatformEngine(): HttpClientEngine

fun HttpRequestBuilder.withApiUrl(baseUrl: String, path: String) {

    url {
        takeFrom(Url(baseUrl)) // Use Ktor's Url to parse the base
        encodedPath = this.encodedPath + path // Append the specific endpoint path
    }
}