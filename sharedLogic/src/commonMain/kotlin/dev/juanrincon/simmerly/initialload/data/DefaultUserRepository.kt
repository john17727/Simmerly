package dev.juanrincon.simmerly.initialload.data

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.initialload.data.remote.UserNetworkClient
import dev.juanrincon.simmerly.initialload.data.remote.dto.UserRatingSummaryDto
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserRecipePreferenceDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserRecipePreferenceEntity
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity

class DefaultUserRepository(
    private val networkClient: UserNetworkClient,
    private val userDao: UserDao,
    private val preferenceDao: UserRecipePreferenceDao,
    private val sessionDataStore: SessionDataStore,
) : UserRepository {

    override suspend fun loadSelf(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getSelf().map { dto ->
            userDao.upsert(dto.toEntity())
            sessionDataStore.setUserId(dto.id)
        }

    override suspend fun currentUserId(): Either<DataError.NetworkError<Unit>, String> =
        sessionDataStore.getUserId()?.right()
            ?: networkClient.getSelf().map { dto ->
                sessionDataStore.setUserId(dto.id)
                dto.id
            }

    override suspend fun loadSelfRatings(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getSelfRatings().map { dto ->
            preferenceDao.upsertAll(dto.ratings.map { it.toPreferenceEntity() })
        }

    override suspend fun loadSelfFavorites(): Either<DataError.NetworkError<Unit>, Unit> =
        networkClient.getSelfFavorites().map { dto ->
            preferenceDao.upsertAll(dto.ratings.map { it.toPreferenceEntity() })
        }

    private fun UserRatingSummaryDto.toPreferenceEntity() = UserRecipePreferenceEntity(
        recipeId = recipeId,
        rating = rating,
        isFavorite = isFavorite,
    )
}
