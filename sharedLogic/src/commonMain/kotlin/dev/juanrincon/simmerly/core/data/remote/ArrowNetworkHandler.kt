package dev.juanrincon.simmerly.core.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException

expect fun <E> mapPlatformException(e: Exception): DataError.NetworkError<E>

suspend inline fun <reified E, reified D> arrowNetworkHandler(call: () -> HttpResponse): Either<DataError.NetworkError<E>, D> =
    either {
        val response = Either.catch { call() }.mapLeft { e ->
            when (e) {
                is UnresolvedAddressException -> raise(DataError.NetworkError.NoInternet)
                is SerializationException -> raise(DataError.NetworkError.Serialization)
                is SocketTimeoutException -> raise(DataError.NetworkError.UnresolvedAddress)
                is HttpRequestTimeoutException -> raise(DataError.NetworkError.RequestTimeout)
                else -> raise(mapPlatformException(e as Exception))
            }
        }.bind()

        when (response.status.value) {
            in 200..299 -> response.body<D>()
            400 -> raise(DataError.NetworkError.BadRequest(response.body<E>()))
            401 -> raise(DataError.NetworkError.Unauthorized)
            409 -> raise(DataError.NetworkError.Conflict)
            408 -> raise(DataError.NetworkError.RequestTimeout)
            413 -> raise(DataError.NetworkError.PayloadTooLarge)
            in 500..599 -> raise(DataError.NetworkError.ServerError)
            else -> raise(DataError.NetworkError.Unknown)
        }
    }