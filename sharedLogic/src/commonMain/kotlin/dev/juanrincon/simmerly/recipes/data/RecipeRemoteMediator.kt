package dev.juanrincon.simmerly.recipes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

@OptIn(ExperimentalPagingApi::class)
class RecipeRemoteMediator(
    private val syncer: RecipeListSyncer,
    private val freshness: Freshness,
    /** Called after a page has been written, so the paging source re-reads the table. */
    private val onPageWritten: () -> Unit = {},
) : RemoteMediator<Int, RecipeSummary>() {

    /** Skips the network on open while the cached list is within its TTL. */
    override suspend fun initialize(): InitializeAction =
        if (freshness.isStale(CacheKeys.RECIPE_LIST, CacheTtl.RECIPE_LIST)) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RecipeSummary>
    ): MediatorResult {
        val cursor: String? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND ->
                syncer.nextCursor() ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            syncer.sync(cursor).fold(
                ifLeft = { MediatorResult.Error(Exception("Network error")) },
                ifRight = { hasMore ->
                    onPageWritten()
                    MediatorResult.Success(endOfPaginationReached = !hasMore)
                }
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
