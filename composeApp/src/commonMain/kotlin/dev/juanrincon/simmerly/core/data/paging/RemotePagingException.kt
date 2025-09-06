package dev.juanrincon.simmerly.core.data.paging

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.RootError

/**
 * A custom Exception used to wrap our domain-specific DataError.NetworkError
 * so that it can be propagated through the Paging 3 library's error channel,
 * which requires a Throwable.
 */
data class RemotePagingException(
    val dataError: RootError
) : Exception("Error from remote mediator: ${dataError::class.simpleName}")
