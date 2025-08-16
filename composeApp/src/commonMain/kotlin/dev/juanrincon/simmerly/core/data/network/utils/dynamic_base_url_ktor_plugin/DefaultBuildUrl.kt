package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath


fun defaultBuildUrl(baseUrl: String, path: String): URLBuilder {
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
