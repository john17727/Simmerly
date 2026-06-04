package dev.juanrincon.simmerly.recipes

import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.right
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

class FakeRecipeRepository : RecipeRepository {

    // --- recipeList ---
    var recipeListPagingData: PagingData<RecipeSummary> = PagingData.empty()

    override fun recipeList(): Flow<PagingData<RecipeSummary>> = flowOf(recipeListPagingData)

    // --- observeAllRecipes ---
    val allRecipesFlow = MutableSharedFlow<List<RecipeSummary>>(replay = 1)

    override fun observeAllRecipes(): Flow<List<RecipeSummary>> = allRecipesFlow

    // --- recipeDetails ---
    private val recipeDetailsFlows = mutableListOf(
        MutableSharedFlow<Either<RecipesError, LoadingResult<RecipeDetail>>>(replay = 1)
    )
    private var recipeDetailsCallCount = 0

    override fun recipeDetails(id: String): Flow<Either<RecipesError, LoadingResult<RecipeDetail>>> {
        val flow = if (recipeDetailsCallCount < recipeDetailsFlows.size) {
            recipeDetailsFlows[recipeDetailsCallCount]
        } else {
            MutableSharedFlow<Either<RecipesError, LoadingResult<RecipeDetail>>>(replay = 1)
                .also { recipeDetailsFlows.add(it) }
        }
        recipeDetailsCallCount++
        return flow
    }

    fun recipeDetailsFlow(index: Int = 0) = recipeDetailsFlows[index]

    // --- comments ---
    val commentsFlow = MutableSharedFlow<List<Comment>>(replay = 0)

    override fun comments(recipeId: String): Flow<List<Comment>> = commentsFlow

    // --- addComment ---
    var addCommentResult: Either<RecipesError, Unit> = Unit.right()
    var lastAddCommentCall: Pair<String, String>? = null

    override suspend fun addComment(recipeId: String, text: String): Either<RecipesError, Unit> {
        lastAddCommentCall = recipeId to text
        return addCommentResult
    }

    // --- updateSettings ---
    var updateSettingsResult: Either<RecipesError, Unit> = Unit.right()
    var lastUpdateSettingsCall: Pair<String, Settings>? = null

    override suspend fun updateSettings(
        recipeId: String,
        settings: Settings
    ): Either<RecipesError, Unit> {
        lastUpdateSettingsCall = recipeId to settings
        return updateSettingsResult
    }

    // --- observe flows ---
    val recentlyViewedFlow = MutableSharedFlow<List<RecipeSummary>>(replay = 0)
    val recentQueriesFlow = MutableSharedFlow<List<String>>(replay = 0)

    override fun observeRecentlyViewed(): Flow<List<RecipeSummary>> = recentlyViewedFlow
    override fun observeRecentSearchQueries(): Flow<List<String>> = recentQueriesFlow

    // --- fire-and-forget tracking ---
    val recordedRecipeViews = mutableListOf<String>()
    val recordedSearchQueries = mutableListOf<String>()

    override suspend fun recordRecipeView(recipeId: String) {
        recordedRecipeViews.add(recipeId)
    }

    override suspend fun recordSearchQuery(query: String) {
        recordedSearchQueries.add(query)
    }
}
