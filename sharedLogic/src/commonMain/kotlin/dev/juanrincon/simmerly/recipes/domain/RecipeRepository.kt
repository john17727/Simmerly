package dev.juanrincon.simmerly.recipes.domain

import androidx.paging.PagingData
import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

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

    /** Sets the logged-in user's personal rating for a recipe (`null` clears it). Reflected back
     * through [recipeDetails] via `user_recipe_preferences`, same as a favorite toggle. Kept
     * separate from [recordRecipeMade] — unlike last-made and the timeline entry, a rating is
     * conditional (a cook may finish without rating), so it isn't part of "the recipe was made". */
    suspend fun setRating(recipeId: String, rating: Double?): Either<RecipesError, Unit>

    /**
     * Records that the recipe was just cooked: updates its last-made timestamp and adds a
     * matching entry to its Mealie timeline, with [note] (if any) as the entry's message. These
     * are two Mealie API calls, but always fire together for the same reason — there is no case
     * in this app where you'd want one without the other — so they're one repository operation.
     * Both are attempted even if one fails; the result is a failure if either was.
     */
    suspend fun recordRecipeMade(recipeId: String, timestamp: Instant, note: String?): Either<RecipesError, Unit>

    fun observeRecentlyViewed(): Flow<List<RecipeSummary>>

    suspend fun recordRecipeView(recipeId: String)

    fun observeRecentSearchQueries(): Flow<List<String>>

    suspend fun recordSearchQuery(query: String)
}
