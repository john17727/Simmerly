package dev.juanrincon.simmerly.core.data

import arrow.core.Either

/**
 * The one shape of an optimistic write: change local state at once so the UI reacts immediately,
 * ask the server, and undo the local change if the server says no.
 *
 * 1. [apply] changes the local state.
 * 2. [remote] makes the request.
 * 3. On [Either.Left], [rollback] restores what [apply] changed and the error is mapped with
 *    [mapError]. On [Either.Right], [onSuccess] gets the server's answer, e.g. to fold in values
 *    the server computed.
 *
 * The caller captures whatever [rollback] needs (a previous value, a snapshot) before calling,
 * because only it knows what "before" is. [rollback] runs only for a failed request: an exception
 * from [apply], [remote] or [onSuccess] propagates untouched.
 */
suspend fun <L, R, E> optimistic(
    apply: suspend () -> Unit,
    remote: suspend () -> Either<L, R>,
    rollback: suspend () -> Unit,
    onSuccess: suspend (R) -> Unit = {},
    mapError: (L) -> E,
): Either<E, R> {
    apply()
    val result = remote()
    result.onLeft { rollback() }
    result.onRight { onSuccess(it) }
    return result.mapLeft(mapError)
}
