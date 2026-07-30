package dev.juanrincon.simmerly.core.data.remote

import app.tracktion.core.domain.util.DataError
import platform.Foundation.NSError
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorTimedOut

actual fun <E> mapPlatformException(e: Exception): DataError.NetworkError<E> =
    DataError.NetworkError.Unknown