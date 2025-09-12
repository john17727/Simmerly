package dev.juanrincon.simmerly.recipes.data

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.fold
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.local.recipe.IngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.mappers.toPaginationData
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.data.store.RecipeStore
import dev.juanrincon.simmerly.recipes.data.store.RecipeStoreFactory
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

@OptIn(ExperimentalStoreApi::class)
class SimmerlyRecipeRepository(
    private val networkClient: RecipeNetworkClient,
    private val database: SimmerlyDatabase,
    private val sessionDataStore: SessionDataStore,
) : RecipeRepository {
    private val recipeDao = database.recipeDao()

    private val store: RecipeStore = RecipeStoreFactory(networkClient, database, sessionDataStore).create()

    override fun recipes(): Flow<List<RecipeSummary>> = sessionDataStore.observeServerAddress()
        .combine(recipeDao.observeRecipeList()) { address, recipes ->
            recipes.map { it.toDomain(address) }
        }

    override suspend fun loadRecipes(
        page: Int,
        perPage: Int,
        refresh: Boolean
    ): Result<PaginationData, RecipesError> {
        if (refresh) {
            recipeDao.clearAll()
        }
        return networkClient.getRecipes(page, perPage).fold(
            onSuccess = { response ->
                recipeDao.upsertAll(response.items.map { it.toEntity() })
                Result.Success(response.toPaginationData())
            },
            onFailure = {
                Result.Error(RecipesError.FetchError)
            }
        )

    }

    override suspend fun getRecipe(id: String): Result<RecipeDetail, RecipesError> {
        TODO("Not yet implemented")
    }

    override fun recipeDetails(id: String): Flow<Result<LoadingResult<RecipeDetail>, RecipesError>> = store.stream(StoreReadRequest.fresh(id)).map { response ->
        when (response) {
            is StoreReadResponse.Data<*> -> Result.Success(LoadingResult.Loaded(response.value as RecipeDetail))
            is StoreReadResponse.Error.Custom<*> -> Result.Error(RecipesError.FetchError)
            is StoreReadResponse.Error.Exception,
            is StoreReadResponse.Error.Message -> Result.Error(RecipesError.UnknownError)
            is StoreReadResponse.Loading -> Result.Success(LoadingResult.Loading)
            else -> throw IllegalStateException()
        }
    }
}