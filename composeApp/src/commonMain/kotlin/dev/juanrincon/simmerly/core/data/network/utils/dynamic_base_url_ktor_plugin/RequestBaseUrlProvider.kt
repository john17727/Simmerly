package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.parsing.ParseException
import io.ktor.http.takeFrom
import io.ktor.utils.io.KtorDsl

fun BaseUrlConfig.request(block: RequestBaseUrlConfig.() -> Unit = { buildUrl = null }) {
    with(RequestBaseUrlConfig().apply(block)) {
        this@request.provider = RequestBaseUrlProvider(buildUrl)
    }
}

@KtorDsl
class RequestBaseUrlConfig {
    internal var buildUrl: ((String, String) -> URLBuilder)? = null

    fun buildUrl(block: ((String, String) -> URLBuilder)?) {
        buildUrl = block
    }
}

class RequestBaseUrlProvider(
    private val buildUrl: ((String, String) -> URLBuilder)?
) : BaseUrlProvider {
    override suspend fun buildBaseUrl(request: HttpRequestBuilder) {
        val overrideUrlString = request.attributes.getOrNull(BaseUrlOverride)
        val originalRequestPath = request.url.encodedPath
        if (overrideUrlString != null) {
            try {
                val newUrl = if (buildUrl != null) {
                    buildUrl(overrideUrlString, originalRequestPath)
                } else {
                    defaultBuildUrl(
                        overrideUrlString,
                        originalRequestPath
                    )
                }
                request.url.takeFrom(newUrl) // Apply the rewritten URL
            } catch (e: ParseException) {
                println("Warning: Malformed dynamic base URL: ${e.message}")
            }
        }
    }
}