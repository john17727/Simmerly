package dev.juanrincon.simmerly.recipes.domain

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun recipes(): Flow<List<RecipeSummary>>

    fun comments(recipeId: String): Flow<List<Comment>>

    suspend fun loadRecipes(page: Int, perPage: Int = 50, refresh: Boolean = false): Result<PaginationData, RecipesError>

    fun recipeDetails(id: String): Flow<Result<LoadingResult<RecipeDetail>, RecipesError>>

    suspend fun addComment(recipeId: String, text: String): Result<Unit, RecipesError>
}