package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.takeFrom

class BaseUrlConfig {
    // Synchronous provider; plugin itself does no I/O
    var provider: BaseUrlProvider? = null

    /**
     * Controls whether the plugin rewrites requests that already have a full/absolute URL (i.e., they include a host).
     *
     * **Default behavior (`false`):**
     *  - Only relative URLs (e.g., `/users`) are rewritten.
     *  - Absolute URLs (e.g., `https://other.com/users`) are left untouched unless a per-request override is set.
     *
     * **When to set to `true`:**
     *  - If you want the plugin to force all requests (even those specifying a host) to use the base URL provided by the [provider], unless explicitly skipped via request attributes.
     *
     * Example:
     *  - Base URL from provider: `https://api.example.com`
     *  - Original request URL: `https://other.com/foo`
     *  - If `rewriteAbsolute = false` (default): Request URL remains `https://other.com/foo`.
     *  - If `rewriteAbsolute = true`: Request URL becomes `https://api.example.com/foo`.
     */
    var rewriteAbsolute: Boolean = false

    /**
     * Determines whether a request's URL should be rewritten by the plugin.
     *
     * This allows for selective URL rewriting. For example, you can target requests under a
     * specific path (e.g., `/api`) or exclude certain requests (e.g., health checks, analytics).
     *
     * Example:
     * - shouldRewrite = { it.url.encodedPath.startsWith("/api") }
     * - This would rewrite `/api/users` but not `/metrics`.
     *
     * Defaults to `true` (rewrite all matching requests).
     */
    var shouldRewrite: (HttpRequestBuilder) -> Boolean = { true }

    /**
     * Controls how the base path and the request path are combined.
     *
     * Available options:
     * - [PathMode.Append]: Concatenates the base path with the request path (e.g., `base path + request path`).
     * - [PathMode.Replace]: Uses only the base's path, ignoring the request path.
     * - [PathMode.KeepRequestOnly]: Ignores the base's path and uses only the request path.
     *
     * Examples (base host https://api.example.com, base path /v1, request path /users):
     * - [PathMode.Append] → `https://api.example.com/v1/users`
     * - [PathMode.Replace] → `https://api.example.com/v1`
     * - [PathMode.KeepRequestOnly] → `https://api.example.com/users`
     *
     * Notes:
     * - The plugin automatically handles slashes to ensure a single leading slash, avoid double slashes, and remove trailing slashes (unless the path is the root `/`).
     *
     * Default value: [PathMode.Append]
     */
    var pathMode: PathMode = PathMode.Append

    /**
     * Controls whether WebSocket (WS/WSS) protocols are preserved when rewriting URLs.
     *
     * **Importance:**
     *  - Standard HTTP/HTTPS base URLs (from the [provider] or per-request overrides) might not be suitable for WebSocket connections.
     *  - If an original request uses `ws://` or `wss://`, this flag ensures the rewritten URL maintains the WebSocket protocol, even if the base URL uses `http://` or `https://`.
     *
     * Example:
     *  - Original request URL: `wss://old.example.com/chat`
     *  - Base URL from provider: `https://api.example.com` (note: HTTPS)
     *
     *  - If `keepWebSocketProtocol = true` (default):
     *    Rewritten URL will be `wss://api.example.com/chat` (protocol remains `wss`).
     *
     *  - If `keepWebSocketProtocol = false`:
     *    Rewritten URL will be `https://api.example.com/chat` (protocol changes to `https`, no longer a WebSocket URL).
     */
    var keepWebSocketProtocol: Boolean = true

    /**
     * Defines the set of URL schemes (protocols) that this plugin is permitted to rewrite.
     *
     * This acts as a safety mechanism to prevent the plugin from unintentionally modifying URLs
     * with schemes it's not designed to handle, such as `file:`, `data:`, or custom application schemes.
     *
     * **Default Value:** `{ http, https, ws, wss }`
     *
     * Example:
     *  - If `allowedSchemes` is `{ URLProtocol.HTTP, URLProtocol.HTTPS }`
     *  - And a request is made to `file:///path/to/resource`
     *  - Then the plugin will not attempt to rewrite this URL, as `file` is not in the `allowedSchemes`.
     */
    var allowedSchemes: Set<URLProtocol> =
        setOf(URLProtocol.HTTP, URLProtocol.HTTPS, URLProtocol.WS, URLProtocol.WSS)
}

val DynamicBaseUrl: ClientPlugin<BaseUrlConfig> =
    createClientPlugin("DynamicBaseUrl", ::BaseUrlConfig) {
        val provider = pluginConfig.provider
        val shouldRewrite = pluginConfig.shouldRewrite
        val allowedSchemes = pluginConfig.allowedSchemes
        val rewriteAbsolute = pluginConfig.rewriteAbsolute
        val keepWebSocketProtocol = pluginConfig.keepWebSocketProtocol
        val pathMode = pluginConfig.pathMode

        // Recommended: install after DefaultRequest
        onRequest { request, _ ->
            // Per-call opt-out
            if (request.attributes.getOrNull(SkipDynamicBaseUrl) == true) return@onRequest
            if (!shouldRewrite(request)) return@onRequest
            if (request.url.protocol !in allowedSchemes) return@onRequest

            // Prefer typed Url override, then String override (parsed safely)
            val perRequestBase: Url? =
                request.attributes.getOrNull(BaseUrlOverrideUrl)
                    ?: request.attributes.getOrNull(BaseUrlOverride)?.let { s ->
                        runCatching { Url(s) }.getOrNull()
                    }

            val isAbsolute = request.url.host.isNotEmpty()
            if (perRequestBase == null && isAbsolute && !rewriteAbsolute) {
                return@onRequest
            }

            val base = perRequestBase ?: provider?.resolveBase(request) ?: return@onRequest

            val protocolOverride =
                if (keepWebSocketProtocol &&
                    (request.url.protocol == URLProtocol.WS || request.url.protocol == URLProtocol.WSS)
                ) request.url.protocol else null

            val rebased = rebaseUrlPreservingQueryAndFragment(
                original = request.url,
                base = base,
                pathMode = pathMode,
                protocolOverride = protocolOverride
            )

            request.url.takeFrom(rebased)
        }
    }