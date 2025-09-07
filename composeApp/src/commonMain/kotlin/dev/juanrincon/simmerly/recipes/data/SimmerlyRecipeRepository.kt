package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.data.paging.NetworkRemoteMediator
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class SimmerlyRecipeRepository(
    private val networkClient: RecipeNetworkClient,
    private val database: SimmerlyDatabase
) : RecipeRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getRecipes(pageSize: Int): Flow<PagingData<RecipeSummary>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false
        ),
        remoteMediator = NetworkRemoteMediator(
            remoteQuery = { page, perPage ->
                networkClient.getRecipes(page, perPage)
            },
            saveRemoteData = { loadType, response ->
                val entities = response.items.map { it.toEntity() }
                val keys = response.items.map { recipe ->
                    RecipeRemoteKey(
                        recipe.id,
                        response.previous,
                        response.next,
                        Clock.System.now()
                    )
                }

                database.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        if (loadType == LoadType.REFRESH) {
                            database.recipeDao().clearAll()
                            database.recipeRemoteKeyDao().clearRemoteKeys()
                        }
                        database.recipeDao().upsertAll(entities)
                        database.recipeRemoteKeyDao().upsertAll(keys)
                    }
                }
            },
            isEndOfPaginationReached = { networkResponse ->
                // Logic to check if you're at the last page
                networkResponse.next == null
            },
            checkInvalidation = {
                val lastUpdated =
                    database.recipeRemoteKeyDao().getCreationTime()?.toEpochMilliseconds() ?: 0L
                (Clock.System.now().toEpochMilliseconds() - lastUpdated) > CACHE_TIMEOUT_MILLIS
            },
            getNextPageKey = { lastItem ->
                database.recipeRemoteKeyDao().getRemoteKeyByRecipeId(lastItem?.id ?: "")?.nextKey
            }
        ),
        pagingSourceFactory = { database.recipeDao().getRecipesPaged() }
    ).flow
        .map { pagingData ->
            pagingData.map { recipeEntity ->
                recipeEntity.toDomain()
            }
        }

    override suspend fun getRecipe(id: String): Result<RecipeDetail, LoginError.NetworkError> {
        TODO("Not yet implemented")
    }

    companion object {
        const val CACHE_TIMEOUT_MILLIS = 3_600_000L
    }
}