package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.DataError
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorTimedOut

actual fun <E> mapPlatformException(e: Exception): DataError.NetworkError<E> {
    val cause = e.cause
    if (cause is NSError) {
        if (cause.domain == NSURLErrorDomain) {
            return when (cause.code) {
                NSURLErrorTimedOut -> DataError.NetworkError.RequestTimeout
                NSURLErrorCannotConnectToHost,
                NSURLErrorNetworkConnectionLost -> DataError.NetworkError.NoInternet
                // Add more mappings as needed
                else -> DataError.NetworkError.Unknown
            }
        }
    }
    return DataError.NetworkError.Unknown
}