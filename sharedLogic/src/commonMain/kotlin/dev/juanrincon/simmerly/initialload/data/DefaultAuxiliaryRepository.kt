package dev.juanrincon.simmerly.initialload.data

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
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
) : AuxiliaryRepository {

    override suspend fun loadTags(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getTags().map { dto ->
            tagDao.upsertAll(dto.items.map { it.toEntity() })
        }

    override suspend fun loadCategories(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getCategories().map { dto ->
            categoryDao.upsertAll(dto.items.map { it.toEntity() })
        }

    override suspend fun loadTools(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getTools().map { dto ->
            toolDao.upsertAll(dto.items.map { it.toEntity() })
        }

    override suspend fun loadUnits(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getUnits().map { dto ->
            unitDao.upsertAll(dto.items.map { it.toEntity() })
        }
}
