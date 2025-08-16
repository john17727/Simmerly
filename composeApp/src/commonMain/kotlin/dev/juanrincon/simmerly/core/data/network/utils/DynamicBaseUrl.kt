package dev.juanrincon.simmerly.core.data.network.utils

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.parsing.ParseException
import io.ktor.http.takeFrom
import io.ktor.util.AttributeKey

val BaseUrlOverride = AttributeKey<String>("BaseUrlOverride")

class DynamicBaseUrlConfig {
    var dynamicBaseUrlProvider: (() -> String?)? = null

    /**
     * Optional: A custom function to combine a base URL string and a request path string
     * into a URLBuilder. If not provided, a default mechanism is used.
     * The function should handle potential ParseExceptions for the baseUrlString.
     */
    var buildUrl: ((String, String) -> URLBuilder)? = null
}

val DynamicBaseUrl: ClientPlugin<DynamicBaseUrlConfig> =
    createClientPlugin("DynamicBaseUrl", ::DynamicBaseUrlConfig) {

        val provider = pluginConfig.dynamicBaseUrlProvider
        val buildUrl = pluginConfig.buildUrl

        onRequest { request, _ ->
            // Check for per-request override first
            val overrideUrlString = request.attributes.getOrNull(BaseUrlOverride)
            val originalRequestPath = request.url.encodedPath
            if (overrideUrlString != null) {
                try {
                    val newUrl = if (buildUrl != null) {
                        buildUrl(overrideUrlString, originalRequestPath)
                    } else {
                        defaultBuildUrl(overrideUrlString, originalRequestPath)
                    }
                    request.url.takeFrom(newUrl) // Apply the rewritten URL
                } catch (e: ParseException) {
                    println("Warning: Malformed dynamic base URL: ${e.message}")
                }
                return@onRequest // Continue with the overridden URL
            }

            val dynamicBaseUrlString = provider?.invoke()
            if (dynamicBaseUrlString != null) {
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
    }

private fun defaultBuildUrl(baseUrl: String, path: String): URLBuilder {
    val baseUrlObject = Url(baseUrl)
    val newUrl = URLBuilder(baseUrl)
    val basePathNormalized = baseUrlObject.encodedPath.removeSuffix("/")
    val requestPathNormalized = path.removePrefix("/")

    newUrl.encodedPath = when {
        basePathNormalized.isEmpty() || basePathNormalized == "/" -> "/$requestPathNormalized"
        requestPathNormalized.isEmpty() -> basePathNormalized
        else -> "$basePathNormalized/$requestPathNormalized"
    }.let { if (it.length > 1 && it.endsWith("/")) it.dropLast(1) else it }
        .ifEmpty { "/" }

    return newUrl
}