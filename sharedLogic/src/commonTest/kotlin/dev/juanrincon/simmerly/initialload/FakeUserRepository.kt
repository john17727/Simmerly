package dev.juanrincon.simmerly.initialload

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.initialload.domain.UserRepository

class FakeUserRepository : UserRepository {
    var loadSelfResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var loadSelfRatingsResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var loadSelfFavoritesResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var currentUserIdResult: Either<DataError.NetworkError<Unit>, String> = "user-1".right()

    var loadSelfCallCount = 0
    var loadSelfRatingsCallCount = 0
    var loadSelfFavoritesCallCount = 0
    var currentUserIdCallCount = 0

    override suspend fun loadSelf(): Either<DataError.NetworkError<Unit>, Unit> {
        loadSelfCallCount++
        return loadSelfResult
    }

    override suspend fun loadSelfRatings(): Either<DataError.NetworkError<Unit>, Unit> {
        loadSelfRatingsCallCount++
        return loadSelfRatingsResult
    }

    override suspend fun loadSelfFavorites(): Either<DataError.NetworkError<Unit>, Unit> {
        loadSelfFavoritesCallCount++
        return loadSelfFavoritesResult
    }

    override suspend fun currentUserId(): Either<DataError.NetworkError<Unit>, String> {
        currentUserIdCallCount++
        return currentUserIdResult
    }
}
