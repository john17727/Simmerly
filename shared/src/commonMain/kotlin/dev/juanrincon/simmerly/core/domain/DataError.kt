package app.tracktion.core.domain.util

//sealed interface DataError : Error {
//    enum class NetworkError : DataError {
//        REQUEST_TIMEOUT,
//        BAD_REQUEST,
//        UNAUTHORIZED,
//        CONFLICT,
//        TOO_MANY_REQUESTS,
//        NO_INTERNET,
//        PAYLOAD_TOO_LARGE,
//        SERVER_ERROR,
//        SERIALIZATION,
//        UNKNOWN;
//    }
//
//    enum class LocalError : DataError {
//        DISK_FULL
//    }
//}

sealed interface DataError : Error {
    sealed interface NetworkError<out E> : DataError {
        data object RequestTimeout : NetworkError<Nothing>
        data object UnresolvedAddress : NetworkError<Nothing>
        data class BadRequest<E>(val data: E?) : NetworkError<E>
        data object Unauthorized : NetworkError<Nothing>
        data object Conflict : NetworkError<Nothing>
        data object TooManyRequests : NetworkError<Nothing>
        data object NoInternet : NetworkError<Nothing>
        data object PayloadTooLarge : NetworkError<Nothing>
        data object ServerError : NetworkError<Nothing>
        data object Serialization : NetworkError<Nothing>
        data object Unknown : NetworkError<Nothing>
    }

    enum class LocalError : DataError {
        DISK_FULL
    }
}