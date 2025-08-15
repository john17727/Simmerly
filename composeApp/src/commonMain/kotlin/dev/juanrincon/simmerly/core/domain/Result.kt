package app.tracktion.core.domain.util

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
    companion object {
        fun <D> success(data: D): Result<D, Nothing> = Success(data)
        fun <E : RootError> error(error: E): Result<Nothing, E> = Error(error)
    }
}

inline fun <T, E : RootError, R, X : RootError> Result<T, E>.map(
    mapData: (T) -> R,
    mapError: (E) -> X
): Result<R, X> = when (this) {
    is Result.Error -> Result.Error(mapError(error))
    is Result.Success -> Result.Success(mapData(data))
}

inline fun <T, E : RootError, R> Result<T, E>.mapData(map: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(map(data))
}

inline fun <T, E : RootError, X : RootError> Result<T, E>.mapError(map: (E) -> X): Result<T, X> =
    when (this) {
        is Result.Error -> Result.Error(map(error))
        is Result.Success -> this
    }

fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return mapData { }
}

inline fun <T, E : RootError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : RootError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
    }
}

inline fun <R, T, E : RootError> Result<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R
): R {
    return when (this) {
        is Result.Error -> onFailure(error)
        is Result.Success -> onSuccess(data)
    }
}

typealias EmptyResult<E> = Result<Unit, E>