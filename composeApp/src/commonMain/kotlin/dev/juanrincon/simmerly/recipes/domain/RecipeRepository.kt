package dev.juanrincon.simmerly.recipes.domain

import app.tracktion.core.domain.util.Result
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    /**
     * Unified list API: refresh from network (with optional refresh/clear) then emit DB-backed list.
     * Returns a Flow that starts with Loading, may emit Error on refresh failure, and then Loaded(list + pagination info).
     */
    fun recipeList(
        page: Int,
        perPage: Int = 50,
        refresh: Boolean = false
    ): Flow<Result<LoadingResult<RecipeListResult>, RecipesError>>

    fun comments(recipeId: String): Flow<List<Comment>>

    fun recipeDetails(id: String): Flow<Result<LoadingResult<RecipeDetail>, RecipesError>>

    suspend fun addComment(recipeId: String, text: String): Result<Unit, RecipesError>

    suspend fun updateSettings(recipeId: String, settings: Settings): Result<Unit, RecipesError>
}