package dev.juanrincon.simmerly.recipes.data

import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.fold
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.data.mappers.toEntity
import dev.juanrincon.simmerly.recipes.data.mappers.toPaginationData
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SimmerlyRecipeRepository(
    private val networkClient: RecipeNetworkClient,
    private val recipeDao: RecipeDao,
    private val sessionDataStore: SessionDataStore
) : RecipeRepository {

    override fun recipes(): Flow<List<RecipeSummary>> = sessionDataStore.observeServerAddress()
        .combine(recipeDao.getRecipes()) { address, recipes ->
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
}