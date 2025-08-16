package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.util.appendAll


/**
 * Rebase [original] onto [base], preserving the original query parameters and fragment.
 * - Origin (scheme/host/port/user/password) comes from [base].
 * - Path = base.encodedPath + original.encodedPath (joined safely).
 * - Query & fragment are kept from [original].
 */
fun rebaseUrlPreservingQueryAndFragment(
    original: URLBuilder,
    base: Url
): URLBuilder = URLBuilder(original).apply {
    // Copy origin from base
    protocol = base.protocol
    host = base.host
    port = base.port
    user = base.user
    password = base.password

    // Merge encoded paths
    encodedPath = mergeEncodedPaths(base.encodedPath, original.encodedPath)
}

/**
 * Convenience overload that accepts a String.
 */
fun rebaseUrlPreservingQueryAndFragment(
    original: URLBuilder,
    base: String
): URLBuilder = rebaseUrlPreservingQueryAndFragment(original, Url(base))

/**
 * Joins two encoded paths predictably:
 * - Keeps a single leading slash.
 * - Avoids double slashes.
 * - Avoids trailing slash unless the path is root.
 */
private fun mergeEncodedPaths(basePath: String, requestPath: String): String {
    val p1 = basePath.trimEnd('/')
    val p2 = requestPath.trimStart('/')

    val joined = when {
        p1.isEmpty() || p1 == "/" ->
            if (p2.isEmpty()) "/" else "/$p2"

        p2.isEmpty() ->
            if (p1.startsWith("/")) p1 else "/$p1"

        else ->
            "${if (p1.startsWith("/")) p1 else "/$p1"}/$p2"
    }

    return if (joined.length > 1 && joined.endsWith('/')) joined.dropLast(1) else joined
}
