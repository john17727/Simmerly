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

    /**
     * Keys are plain row offsets, always a multiple of [PagingConfig.pageSize], so the refresh key
     * is just the anchor's row (backed up half a load, to keep context above it) rounded down to
     * a page boundary. The old version derived it from a neighbouring page's key, which landed a
     * page off for the first page.
     */
    override fun getRefreshKey(state: PagingState<Int, RecipeSummary>): Int? =
        state.anchorPosition?.let { anchor ->
            // anchorPosition indexes the loaded items, so add the row the first loaded page starts at.
            val firstPageStart = state.pages.firstOrNull()?.prevKey?.plus(PAGE_SIZE) ?: 0
            val start = (firstPageStart + anchor - state.config.initialLoadSize / 2).coerceAtLeast(0)
            start / PAGE_SIZE * PAGE_SIZE
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
                // Page-sized, not loadSize: a refresh loads several pages at once, and prepends are one.
                prevKey = if (offset == 0) null else (offset - PAGE_SIZE).coerceAtLeast(0),
                nextKey = if (items.size < params.loadSize) null else offset + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        /** Rows per page; every key is a multiple of it. Also the [PagingConfig] page size. */
        const val PAGE_SIZE = 50
    }
}
