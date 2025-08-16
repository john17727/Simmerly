package dev.juanrincon.simmerly.core.data.network

import dev.juanrincon.simmerly.core.domain.network.BaseUrlProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.util.AttributeKey

val BaseUrlOverride = AttributeKey<String>("BaseUrlOverride")

class DynamicBaseUrl(private val baseUrlProvider: BaseUrlProvider) :
    HttpClientPlugin<Unit, DynamicBaseUrl> { // Unit for config, DynamicBaseUrlPlugin for plugin instance

    override val key: AttributeKey<DynamicBaseUrl> = AttributeKey("DynamicBaseUrlPlugin")

    override fun prepare(block: Unit.() -> Unit): DynamicBaseUrl = this
    override fun install(
        plugin: DynamicBaseUrl,
        scope: HttpClient
    ) {
        scope.requestPipeline.intercept(HttpRequestPipeline.State) {
            // Check for per-request override first
            val overrideUrlString = context.attributes.getOrNull(BaseUrlOverride)
            if (overrideUrlString != null) {
                val baseUrl = Url(overrideUrlString) // Parse the dynamic base URL
                val originalRequestPath = context.url.encodedPath
                context.url.takeFrom(createUrl(baseUrl, originalRequestPath)) // Apply the rewritten URL
                proceed() // Continue with the overridden URL
                return@intercept
            }

            val dynamicBaseUrlString = baseUrlProvider.current()
            if (dynamicBaseUrlString != null) {
                val dynamicBaseUrl = Url(dynamicBaseUrlString) // Parse the dynamic base URL
                val originalRequestPath = context.url.encodedPath

                context.url.takeFrom(createUrl(dynamicBaseUrl, originalRequestPath)) // Apply the rewritten URL
            }
            // If dynamicBaseUrlString is null, it will use the fallback from defaultRequest
            // or whatever was set before this interceptor.
            proceed() // Continue with the (potentially modified) request
        }
    }

    private fun createUrl(baseUrl: Url, path: String): URLBuilder {
        // Create a new URLBuilder based on the dynamic base URL
        val newUrl = URLBuilder(baseUrl)
        // Append the original request's path and query
        // Careful with double slashes if dynamicBaseUrl already has a path
        newUrl.encodedPath =
            baseUrl.encodedPath.removeSuffix("/") + "/" + path.removePrefix(
                "/"
            )
        return newUrl
    }
}