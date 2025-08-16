package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath

enum class PathMode { Append, Replace, KeepRequestOnly }

/**
 * Rebase [original] onto [base], preserving the original query parameters and fragment.
 * - Origin (scheme/host/port/user/password) comes from [base].
 * - Path handling controlled by [pathMode].
 * - Query & fragment are kept from [original].
 * - Optionally preserve WS/WSS protocol ([protocolOverride]) if the original was a websocket.
 */
fun rebaseUrlPreservingQueryAndFragment(
    original: URLBuilder,
    base: Url,
    pathMode: PathMode = PathMode.Append,
    protocolOverride: URLProtocol? = null
): URLBuilder = URLBuilder(original).apply {
    protocol = protocolOverride ?: base.protocol
    host = base.host
    port = base.port
    user = base.user
    password = base.password

    encodedPath = when (pathMode) {
        PathMode.Append -> mergeEncodedPaths(base.encodedPath, original.encodedPath)
        PathMode.Replace -> normalizeLeadingSlash(base.encodedPath)
        PathMode.KeepRequestOnly -> normalizeLeadingSlash(original.encodedPath)
    }
}

private fun mergeEncodedPaths(basePath: String, requestPath: String): String {
    val p1 = basePath.trimEnd('/')
    val p2 = requestPath.trimStart('/')
    val joined = when {
        p1.isEmpty() || p1 == "/" -> if (p2.isEmpty()) "/" else "/$p2"
        p2.isEmpty() -> if (p1.startsWith("/")) p1 else "/$p1"
        else -> "${if (p1.startsWith("/")) p1 else "/$p1"}/$p2"
    }
    return if (joined.length > 1 && joined.endsWith('/')) joined.dropLast(1) else joined
}

private fun normalizeLeadingSlash(path: String): String {
    val p = path.ifEmpty { "/" }.let { if (it.startsWith("/")) it else "/$it" }
    return if (p.length > 1 && p.endsWith('/')) p.dropLast(1) else p
}
