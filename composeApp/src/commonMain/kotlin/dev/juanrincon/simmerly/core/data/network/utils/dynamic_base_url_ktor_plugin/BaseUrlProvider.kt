package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.Url

interface BaseUrlProvider {
    /**
     * Return the base Url to rebase against for this request, or null to skip.
     * Keep this non-blocking if possible.
     */
    suspend fun resolveBase(request: HttpRequestBuilder): Url?
}