package dev.juanrincon.simmerly.core.data.paging

import androidx.collection.size
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A generic PagingSource for fetching paginated data from an SQLDelight query.
 *
 * @param T The type of the items to be loaded.
 * @param query A lambda function that takes a limit and an offset and returns a list of items
 *              from the database. This is where you call your SQLDelight query's `executeAsList()`.
 * @param dispatcher The CoroutineContext (typically a background dispatcher like Dispatchers.IO)
 *                   on which the database query should be executed.
 */
class SqlDelightPagingSource<T : Any>(
    private val query: (limit: Int, offset: Int) -> List<T>,
    private val dispatcher: CoroutineContext
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val offset = params.key ?: 0
        val limit = params.loadSize

        val items = withContext(dispatcher) {
            query(limit, offset)
        }

        val nextKey = if (items.size < limit) null else offset + items.size
        val prevKey = if (offset == 0) null else (offset - limit).coerceAtLeast(0)

        LoadResult.Page(
            data = items,
            prevKey = prevKey,
            nextKey = nextKey
        )
    } catch (t: Throwable) {
        LoadResult.Error(t)
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? { // <-- Return type is Int?
        // A standard implementation for getRefreshKey
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize) ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }
}