package dev.juanrincon.simmerly.initialload.domain

import app.tracktion.core.domain.util.DataError
import arrow.core.Either

interface UserRepository {
    suspend fun loadSelf(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadSelfRatings(): Either<DataError.NetworkError<Unit>, Unit>
    suspend fun loadSelfFavorites(): Either<DataError.NetworkError<Unit>, Unit>

    /** The logged-in user's Mealie UUID. Reads the value [loadSelf] already persisted where
     * possible; falls back to fetching and persisting it on demand for a session that predates
     * that persistence (an existing login from before this method existed). */
    suspend fun currentUserId(): Either<DataError.NetworkError<Unit>, String>
}
