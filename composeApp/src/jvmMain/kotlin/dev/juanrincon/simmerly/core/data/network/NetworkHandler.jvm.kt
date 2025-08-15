package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.DataError

actual fun <E> mapPlatformException(e: Exception): DataError.NetworkError<E> = when (e) {
    is java.net.ConnectException,
    is java.net.UnknownHostException -> DataError.NetworkError.UnresolvedAddress

    else -> DataError.NetworkError.Unknown
}