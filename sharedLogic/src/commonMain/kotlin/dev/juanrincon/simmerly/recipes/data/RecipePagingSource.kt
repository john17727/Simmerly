package dev.juanrincon.simmerly.recipes.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserRecipePreferenceDao
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.flow.first

class RecipePagingSource(
    private val recipeDao: RecipeDao,
    private val preferenceDao: UserRecipePreferenceDao,
    private val sessionDataStore: SessionDataStore,
) : PagingSource<Int, RecipeSummary>() {

    override fun getRefreshKey(state: PagingState<Int, RecipeSummary>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(state.config.pageSize)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecipeSummary> {
        val offset = params.key ?: 0
        return try {
            val address = sessionDataStore.observeServerAddress().first()
            val items = recipeDao.getRecipePage(offset, params.loadSize)
            val preferences = preferenceDao.getAll()
            val prefMap = preferences.associateBy { it.recipeId }
            val mapped = items.map {
                it.toDomain(address, isFavorite = prefMap[it.recipe.id]?.isFavorite ?: false)
            }
            LoadResult.Page(
                data = mapped,
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = if (items.size < params.loadSize) null else offset + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
