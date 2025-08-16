package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.AttributeKey

val BaseUrlOverride = AttributeKey<String>("BaseUrlOverride")

class BaseUrlConfig {
    var provider: BaseUrlProvider? = null
}

val DynamicBaseUrl: ClientPlugin<BaseUrlConfig> =
    createClientPlugin("DynamicBaseUrl", ::BaseUrlConfig) {

        val provider = pluginConfig.provider

        onRequest { request, _ ->
           provider?.buildBaseUrl(request)
        }
    }