package dev.juanrincon.simmerly.initialload.domain

import app.tracktion.core.domain.util.DataError
import arrow.core.Either

interface UserRepository {
    suspend fun loadSelf(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadSelfRatings(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadSelfFavorites(): Either<DataError.NetworkError<Unit>, Unit>
}
