package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.parsing.ParseException
import io.ktor.http.takeFrom
import io.ktor.utils.io.KtorDsl

fun BaseUrlConfig.listener(block: ListenerBaseUrlConfig.() -> Unit) {
    with(ListenerBaseUrlConfig().apply(block)) {
        this@listener.provider = ListenerBaseUrlProvider(loadBaseUrls, buildUrl)
    }
}

@KtorDsl
class ListenerBaseUrlConfig {
    internal var loadBaseUrls: suspend () -> String? = { null }
    internal var buildUrl: ((String, String) -> URLBuilder)? = null

    fun loadBaseUrls(block: suspend () -> String?) {
        loadBaseUrls = block
    }

    fun buildUrl(block: ((String, String) -> URLBuilder)?) {
        buildUrl = block
    }
}

class ListenerBaseUrlProvider(
    loadBaseUrls: suspend () -> String?,
    private val buildUrl: ((String, String) -> URLBuilder)?
) : BaseUrlProvider {

    private val baseUrlHolder = BaseUrlHolder(loadBaseUrls)
    override suspend fun buildBaseUrl(request: HttpRequestBuilder) {
        // Check for per-request override first
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
            return // Continue with the overridden URL
        }

        val dynamicBaseUrlString = baseUrlHolder.loadBaseUrl() ?: return
        try {
            val newUrl = if (buildUrl != null) {
                buildUrl(dynamicBaseUrlString, originalRequestPath)
            } else {
                defaultBuildUrl(dynamicBaseUrlString, originalRequestPath)
            }
            request.url.takeFrom(newUrl) // Apply the rewritten URL
        } catch (e: ParseException) {
            println("Warning: Malformed dynamic base URL: ${e.message}")
        }
    }
}