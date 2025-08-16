package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.utils.io.KtorDsl

/**
 * Configures the base URL for individual requests.
 * This allows overriding the global base URL for specific requests.
 *
 * @param block A lambda to configure the request-specific base URL.
 */

fun BaseUrlConfig.override(block: OverrideBaseUrlConfig.() -> Unit = { buildUrl = null }) {
    with(OverrideBaseUrlConfig().apply(block)) {
        this@override.provider = OverrideBaseUrlProvider(buildUrl, buildUrlString)
    }
}

@KtorDsl
class OverrideBaseUrlConfig {
    internal var buildUrl: ((URLBuilder, Url) -> URLBuilder)? = null
    internal var buildUrlString: ((URLBuilder, String) -> URLBuilder)? = null

    /**
     * Sets a lambda that builds the URL using a `Url` object.
     * The lambda receives the current [URLBuilder] and the base [Url] as input,
     * and should return the modified [URLBuilder].
     *
     * @param block A lambda that takes a [URLBuilder] and a [Url] and returns a [URLBuilder].
     */
    fun buildUrl(block: ((URLBuilder, Url) -> URLBuilder)?) { buildUrl = block }
    /**
     * Sets a lambda that builds the URL using a base URL string.
     * The lambda receives the current [URLBuilder] and the base URL [String] as input,
     * and should return the modified [URLBuilder].
     *
     * @param block A lambda that takes a [URLBuilder] and a base URL [String] and returns a [URLBuilder].
     */
    fun buildUrlString(block: ((URLBuilder, String) -> URLBuilder)?) { buildUrlString = block }
}

class OverrideBaseUrlProvider(
    private val buildUrl: ((URLBuilder, Url) -> URLBuilder)?,
    private val buildUrlString: ((URLBuilder, String) -> URLBuilder)?
) : BaseUrlProvider {

    override suspend fun resolveBase(request: HttpRequestBuilder): Url? {
        request.attributes.getOrNull(BaseUrlOverrideUrl)?.let { return it }
        request.attributes.getOrNull(BaseUrlOverride)?.let { s ->
            return runCatching { Url(s) }.getOrNull()
        }
        return null
    }
}