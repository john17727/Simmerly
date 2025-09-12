package dev.juanrincon.simmerly.recipes.domain

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun recipes(): Flow<List<RecipeSummary>>

    suspend fun loadRecipes(page: Int, perPage: Int = 50, refresh: Boolean = false): Result<PaginationData, RecipesError>

    suspend fun getRecipe(id: String): Result<RecipeDetail, RecipesError>

    fun recipeDetails(id: String): Flow<Result<RecipeDetail, RecipesError>>
}