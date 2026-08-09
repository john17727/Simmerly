package dev.juanrincon.simmerly.core.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

expect fun <E> mapPlatformException(e: Exception): DataError.NetworkError<E>

/**
 * Runs [call], maps transport-level exceptions and non-2xx statuses onto [DataError.NetworkError],
 * and returns the raw 2xx [HttpResponse] otherwise — shared by [arrowNetworkHandler] (which decodes
 * a body from it) and [arrowNetworkHandlerNoContent] (which doesn't), so the two can't drift.
 */
@PublishedApi
internal suspend inline fun <reified E> Raise<DataError.NetworkError<E>>.executeRequest(
    call: () -> HttpResponse
): HttpResponse {
    val response = Either.catch { call() }.mapLeft { e ->
        when (e) {
            is UnresolvedAddressException -> raise(DataError.NetworkError.NoInternet)
            is SerializationException -> raise(DataError.NetworkError.Serialization)
            is SocketTimeoutException -> raise(DataError.NetworkError.UnresolvedAddress)
            is HttpRequestTimeoutException -> raise(DataError.NetworkError.RequestTimeout)
            else -> raise(mapPlatformException(e as Exception))
        }
    }.bind()

    return when (response.status.value) {
        in 200..299 -> response
        400 -> raise(DataError.NetworkError.BadRequest(response.body<E>()))
        401 -> raise(DataError.NetworkError.Unauthorized)
        409 -> raise(DataError.NetworkError.Conflict)
        408 -> raise(DataError.NetworkError.RequestTimeout)
        413 -> raise(DataError.NetworkError.PayloadTooLarge)
        in 500..599 -> raise(DataError.NetworkError.ServerError)
        else -> raise(DataError.NetworkError.Unknown)
    }
}

suspend inline fun <reified E, reified D> arrowNetworkHandler(call: () -> HttpResponse): Either<DataError.NetworkError<E>, D> =
    either { executeRequest<E>(call).body<D>() }

/**
 * Like [arrowNetworkHandler], but for endpoints whose success response has no body worth decoding —
 * a 2xx status is the only signal. Never calls `response.body<Unit>()`, so it can't fail on a
 * response that turns out to carry real content despite a spec (or assumption) saying otherwise.
 */
suspend inline fun <reified E> arrowNetworkHandlerNoContent(call: () -> HttpResponse): Either<DataError.NetworkError<E>, Unit> =
    either { executeRequest<E>(call) }