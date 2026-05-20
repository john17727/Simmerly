package dev.juanrincon.simmerly.recipes.domain

import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
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
    ): Flow<Either<RecipesError, LoadingResult<RecipeListResult>>>

    fun comments(recipeId: String): Flow<List<Comment>>

    fun recipeDetails(id: String): Flow<Either<RecipesError, LoadingResult<RecipeDetail>>>

    suspend fun addComment(recipeId: String, text: String): Either<RecipesError, Unit>

    suspend fun updateSettings(recipeId: String, settings: Settings): Either<RecipesError, Unit>

    fun observeRecentlyViewed(): Flow<List<RecipeSummary>>

    suspend fun recordRecipeView(recipeId: String)

    fun observeRecentSearchQueries(): Flow<List<String>>

    suspend fun recordSearchQuery(query: String)
}