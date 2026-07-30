package dev.juanrincon.simmerly.initialload

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.initialload.domain.AuxiliaryRepository

class FakeAuxiliaryRepository : AuxiliaryRepository {
    var loadTagsResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var loadCategoriesResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var loadToolsResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()
    var loadUnitsResult: Either<DataError.NetworkError<Unit>, Unit> = Unit.right()

    var loadTagsCallCount = 0
    var loadCategoriesCallCount = 0
    var loadToolsCallCount = 0
    var loadUnitsCallCount = 0

    override suspend fun loadTags(): Either<DataError.NetworkError<Unit>, Unit> {
        loadTagsCallCount++
        return loadTagsResult
    }

    override suspend fun loadCategories(): Either<DataError.NetworkError<Unit>, Unit> {
        loadCategoriesCallCount++
        return loadCategoriesResult
    }

    override suspend fun loadTools(): Either<DataError.NetworkError<Unit>, Unit> {
        loadToolsCallCount++
        return loadToolsResult
    }

    override suspend fun loadUnits(): Either<DataError.NetworkError<Unit>, Unit> {
        loadUnitsCallCount++
        return loadUnitsResult
    }
}
