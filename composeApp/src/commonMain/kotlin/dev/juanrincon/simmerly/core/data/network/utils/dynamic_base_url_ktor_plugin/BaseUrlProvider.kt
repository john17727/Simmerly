package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder

interface BaseUrlProvider {

    suspend fun buildBaseUrl(request: HttpRequestBuilder)
}