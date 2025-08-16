package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.util.appendAll
import io.ktor.utils.io.KtorDsl

fun BaseUrlConfig.listener(block: ListenerBaseUrlConfig.() -> Unit) {
    with(ListenerBaseUrlConfig().apply(block)) {
        this@listener.provider = ListenerBaseUrlProvider(loadBaseUrls, buildUrl)
    }
}

@KtorDsl
class ListenerBaseUrlConfig {
    internal var loadBaseUrls: suspend () -> String? = { null }
    internal var buildUrl: ((URLBuilder, String) -> URLBuilder)? = null

    fun loadBaseUrls(block: suspend () -> String?) {
        loadBaseUrls = block
    }

    fun buildUrl(block: ((URLBuilder, String) -> URLBuilder)?) {
        buildUrl = block
    }
}

class ListenerBaseUrlProvider(
    loadBaseUrls: suspend () -> String?,
    private val buildUrl: ((URLBuilder, String) -> URLBuilder)?
) : BaseUrlProvider {

    private val baseUrlHolder = BaseUrlHolder(loadBaseUrls)
    override suspend fun buildBaseUrl(request: HttpRequestBuilder) {
        val original = URLBuilder(request.url)
        val override = request.attributes.getOrNull(BaseUrlOverride)
        val baseValue = override ?: baseUrlHolder.loadBaseUrl() ?: return

        val newUrl = (buildUrl?.invoke(original, baseValue))
            ?: rebaseUrlPreservingQueryAndFragment(original,baseValue)

        request.url.takeFrom(newUrl)
    }
}