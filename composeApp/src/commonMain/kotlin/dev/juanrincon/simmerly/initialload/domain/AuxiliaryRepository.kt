package dev.juanrincon.simmerly.initialload.domain

import app.tracktion.core.domain.util.DataError
import arrow.core.Either

interface AuxiliaryRepository {
    suspend fun loadTags(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadCategories(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadTools(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadFoods(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadUnits(): Either<DataError.NetworkError<Unit>, Unit>
}
