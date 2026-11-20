package dev.juanrincon.simmerly.initialload.data

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.initialload.data.remote.AuxiliaryNetworkClient
import dev.juanrincon.simmerly.initialload.domain.AuxiliaryRepository
import dev.juanrincon.simmerly.recipes.data.local.recipe.CategoryDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.FoodDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.TagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.ToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UnitDao
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity

class DefaultAuxiliaryRepository(
    private val networkClient: AuxiliaryNetworkClient,
    private val tagDao: TagDao,
    private val categoryDao: CategoryDao,
    private val toolDao: ToolDao,
    private val foodDao: FoodDao,
    private val unitDao: UnitDao,
    private val freshness: Freshness,
) : AuxiliaryRepository {

    override suspend fun loadTags(): Either<DataError.NetworkError<Unit>, Unit> =
        loadIfStale("tags") {
            networkClient.getTags().map { dto -> tagDao.upsertAll(dto.items.map { it.toEntity() }) }
        }

    override suspend fun loadCategories(): Either<DataError.NetworkError<Unit>, Unit> =
        loadIfStale("categories") {
            networkClient.getCategories().map { dto ->
                categoryDao.upsertAll(dto.items.map { it.toEntity() })
            }
        }

    override suspend fun loadTools(): Either<DataError.NetworkError<Unit>, Unit> =
        loadIfStale("tools") {
            networkClient.getTools().map { dto -> toolDao.upsertAll(dto.items.map { it.toEntity() }) }
        }

    override suspend fun loadUnits(): Either<DataError.NetworkError<Unit>, Unit> =
        loadIfStale("units") {
            networkClient.getUnits().map { dto -> unitDao.upsertAll(dto.items.map { it.toEntity() }) }
        }

    /**
     * Reference data changes rarely, so each dataset is refetched only once its TTL has passed;
     * a failed fetch leaves it stale so the next start tries again.
     */
    private suspend fun loadIfStale(
        name: String,
        fetch: suspend () -> Either<DataError.NetworkError<Unit>, Unit>,
    ): Either<DataError.NetworkError<Unit>, Unit> {
        val key = CacheKeys.referenceData(name)
        if (!freshness.isStale(key, CacheTtl.REFERENCE_DATA)) return Unit.right()
        return fetch().onRight { freshness.markSynced(key) }
    }
}
