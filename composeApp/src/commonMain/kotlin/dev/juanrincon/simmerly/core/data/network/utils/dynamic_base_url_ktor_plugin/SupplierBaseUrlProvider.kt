package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.utils.io.KtorDsl

/**
 * Configures a [BaseUrlProvider] that dynamically provides the base URL using a listener-based approach.
 * This allows the base URL to be updated at runtime.
 *
 * @param block A lambda that configures the [SupplierBaseUrlConfig].
 */

fun BaseUrlConfig.supplier(block: SupplierBaseUrlConfig.() -> Unit) {
    with(SupplierBaseUrlConfig().apply(block)) {
        this@supplier.provider = SupplierBaseUrlProvider(loadBaseUrl, buildUrl, buildUrlString)
    }
}

@KtorDsl
class SupplierBaseUrlConfig {
    internal var loadBaseUrl: suspend () -> String? = { null }

    // Prefer typed hook
    internal var buildUrl: ((original: URLBuilder, base: Url) -> URLBuilder)? = null
    // Legacy hook kept for compatibility
    internal var buildUrlString: ((original: URLBuilder, base: String) -> URLBuilder)? = null

    /**
     * Specifies a lambda that will be called to provide the base URL.
     * The lambda should return a [String] representing the base URL, or `null` if no base URL is available.
     *
     * @param block The lambda to provide the base URL.
     */
    fun loadBaseUrl(block: suspend () -> String?) { loadBaseUrl = block }

    /**
     * Specifies a lambda that will be called to build the final URL.
     * The lambda receives the original [URLBuilder] and the base [Url] as parameters, and should return a new [URLBuilder].
     *
     * @param block The lambda to build the final URL.
     */
    fun buildUrl(block: ((URLBuilder, Url) -> URLBuilder)?) { buildUrl = block }

    /**
     * Specifies a lambda that will be called to build the final URL (legacy version).
     * The lambda receives the original [URLBuilder] and the base URL as a [String] as parameters, and should return a new [URLBuilder].
     * This is kept for compatibility purposes. Prefer using [buildUrl] with a typed [Url].
     *
     * @param block The lambda to build the final URL.
     */
    fun buildUrlString(block: ((URLBuilder, String) -> URLBuilder)?) { buildUrlString = block }
}

class SupplierBaseUrlProvider(
    loadBaseUrl: suspend () -> String?,
    private val buildUrl: ((URLBuilder, Url) -> URLBuilder)?,
    private val buildUrlString: ((URLBuilder, String) -> URLBuilder)?
) : BaseUrlProvider {

    private val holder = BaseUrlHolder(loadBaseUrl)

    override suspend fun resolveBase(request: HttpRequestBuilder): Url? {
        // Per-request override wins
        request.attributes.getOrNull(BaseUrlOverrideUrl)?.let { return it }
        request.attributes.getOrNull(BaseUrlOverride)?.let { s ->
            runCatching { Url(s) }.getOrNull()?.let { return it }
        }
        // Fallback to loaded value
        return holder.loadBaseUrl()?.let { runCatching { Url(it) }.getOrNull() }
    }
}