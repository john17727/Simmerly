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
    private val localQuery: suspend (D) -> Unit,
    private val remoteToLocal: (N) -> D,
    private val isEndOfPaginationReached: (N) -> Boolean,
    private val clearLocalData: suspend () -> Unit
) : RemoteMediator<Int, D>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, D>
    ): MediatorResult {
        val pageToLoad = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
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
        return try {
            val result = remoteQuery(pageToLoad, state.config.pageSize)

            result.fold(
                onSuccess = { networkResponse ->
                    val localData = remoteToLocal(networkResponse)

                    if (loadType == LoadType.REFRESH) {
                        clearLocalData()
                    }
                    localQuery(localData)

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