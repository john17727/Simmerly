package dev.juanrincon.simmerly.recipes.domain

import androidx.paging.PagingData
import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun recipeList(): Flow<PagingData<RecipeSummary>>

    fun observeAllRecipes(): Flow<List<RecipeSummary>>

    /**
     * Loads the next cursor page of recipes into local storage, or the first page if none has
     * been loaded yet. Returns whether more pages remain after this load.
     */
    suspend fun loadNextRecipePage(): Either<RecipesError, Boolean>

    /** Clears cached recipes and reloads the first page. Returns whether more pages remain. */
    suspend fun refreshRecipeList(): Either<RecipesError, Boolean>

    fun comments(recipeId: String): Flow<List<Comment>>

    fun recipeDetails(id: String): Flow<Either<RecipesError, LoadingResult<RecipeDetail>>>

    suspend fun addComment(recipeId: String, text: String): Either<RecipesError, Unit>

    suspend fun updateSettings(recipeId: String, settings: Settings): Either<RecipesError, Unit>

    fun observeRecentlyViewed(): Flow<List<RecipeSummary>>

    suspend fun recordRecipeView(recipeId: String)

    fun observeRecentSearchQueries(): Flow<List<String>>

    suspend fun recordSearchQuery(query: String)
}
