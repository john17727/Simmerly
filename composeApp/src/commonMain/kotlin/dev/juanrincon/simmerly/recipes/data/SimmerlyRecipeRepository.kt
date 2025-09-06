package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.core.data.paging.NetworkRemoteMediator
import dev.juanrincon.simmerly.core.data.paging.SqlDelightPagingSource
import dev.juanrincon.simmerly.recipes.data.local.RecipeDao
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SimmerlyRecipeRepository(
    private val networkClient: RecipeNetworkClient,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getRecipes(pageSize: Int): Flow<PagingData<RecipeSummary>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false
        ),
        remoteMediator = NetworkRemoteMediator(
            remoteQuery = { page, perPage ->
                networkClient.getRecipes(page, perPage) // Returns your custom Result
            },
            localQuery = { recipeEntities ->
                recipeDao.upsertAll(recipeEntities)
            },
            remoteToLocal = { networkResponse ->
                // Map network DTOs to local DB entities
                networkResponse.items.map { it.toEntity() }
            },
            isEndOfPaginationReached = { networkResponse ->
                // Logic to check if you're at the last page
                networkResponse.next == null
            },
            clearLocalData = {
                recipeDao.clearAll()
            }
        ),
        pagingSourceFactory = {
            SqlDelightPagingSource(
                query = { limit, offset ->
                    recipeDao.getAll(limit, offset)
                },
                dispatcher = Dispatchers.IO
            )
        }
    ).flow
        .map { pagingData ->
            pagingData.map { recipeEntity ->
                recipeEntity.toDomain()
            }
        }

    override suspend fun getRecipe(id: String): Result<RecipeDetail, LoginError.NetworkError> {
        TODO("Not yet implemented")
    }
}