package dev.juanrincon.simmerly.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import app.tracktion.core.domain.util.Result
import app.tracktion.core.domain.util.RootError
import app.tracktion.core.domain.util.fold
import okio.IOException

@OptIn(ExperimentalPagingApi::class)
class NetworkRemoteMediator<N, D : Any, E : RootError>(
    private val remoteQuery: suspend (page: Int, perPage: Int) -> Result<N, E>,
    private val saveRemoteData: suspend (loadType: LoadType, response: N) -> Unit,
    private val isEndOfPaginationReached: (N) -> Boolean,
    private val checkInvalidation: (suspend () -> Boolean)? = null,
    private val getNextPageKey: (suspend (D?) -> Int?)? = null,
) : RemoteMediator<Int, D>() {

    override suspend fun initialize(): InitializeAction {
        return checkInvalidation?.let { isCacheStale ->
            if (isCacheStale.invoke()) {
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        } ?: super.initialize()
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, D>
    ): MediatorResult {
        val pageToLoad = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                if (getNextPageKey != null) {
                    val lastItem = state.lastItemOrNull()
                    getNextPageKey.invoke(lastItem)
                        ?: return MediatorResult.Success(lastItem == null)
                } else {
                    // This is the core of the stateless strategy.
                    // We calculate the next page number based on the number of items
                    // already loaded into the database.
                    val itemsLoaded = state.pages.sumOf { it.data.size }

                    // If no items are loaded, it means we should fetch the first page.
                    // This can happen if the initial REFRESH fails, and the user tries to scroll.
                    if (itemsLoaded == 0) {
                        1
                    } else {
                        // Page number is 1-based, so we calculate it from the 0-based item count.
                        (itemsLoaded / state.config.pageSize) + 1
                    }
                }
            }
        }
        return try {
            val result = remoteQuery(pageToLoad, state.config.pageSize)

            result.fold(
                onSuccess = { networkResponse ->

                    saveRemoteData(loadType, networkResponse)

                    MediatorResult.Success(
                        endOfPaginationReached = isEndOfPaginationReached(networkResponse)
                    )
                },
                onFailure = { error ->
                    MediatorResult.Error(RemotePagingException(error))
                }
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        }
    }
}