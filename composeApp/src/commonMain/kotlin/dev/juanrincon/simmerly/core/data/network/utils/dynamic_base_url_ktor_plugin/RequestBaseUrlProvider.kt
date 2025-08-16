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
    internal var buildUrl: ((URLBuilder, String) -> URLBuilder)? = null

    fun buildUrl(block: ((URLBuilder, String) -> URLBuilder)?) {
        buildUrl = block
    }
}

class RequestBaseUrlProvider(
    private val buildUrl: ((URLBuilder, String) -> URLBuilder)?
) : BaseUrlProvider {
    override suspend fun buildBaseUrl(request: HttpRequestBuilder) {
        val original = URLBuilder(request.url)
        val override = request.attributes.getOrNull(BaseUrlOverride)
        val baseValue = override ?: return

        val newUrl = (buildUrl?.invoke(original, baseValue))
            ?: rebaseUrlPreservingQueryAndFragment(original,baseValue)

        request.url.takeFrom(newUrl)
    }
}