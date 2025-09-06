package dev.juanrincon.simmerly.core.data.paging

import androidx.collection.size
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A generic PagingSource for fetching paginated data from an SQLDelight query.
 * This is the modern, recommended approach for KMP since androidx.paging:paging-common
 * became a multiplatform library.
 *
 * @param T The type of the items to be loaded.
 * @param pageSize The number of items to load per page. This is used to calculate prevKey
 *                 and to cap the initial load size.
 * @param query A lambda function that takes a limit and an offset and returns a list of items
 *              from the database. This is where you call your SQLDelight query's `executeAsList()`.
 * @param dispatcher The CoroutineContext (typically a background dispatcher like Dispatchers.IO)
 *                   on which the database query should be executed.
 */
class SqlDelightPagingSource<T : Any>(
    private val pageSize: Int,
    private val query: (limit: Long, offset: Long) -> List<T>,
    private val dispatcher: CoroutineContext
) : PagingSource<Long, T>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, T> = try {
        val offset = params.key ?: 0L
        val limit = params.loadSize.toLong().coerceAtMost(pageSize.toLong())

        val items = withContext(dispatcher) {
            query(limit, offset)
        }

        val nextKey = if (items.size < limit) null else offset + items.size
        val prevKey = if (offset == 0L) null else (offset - pageSize).coerceAtLeast(0L)

        LoadResult.Page(
            data = items,
            prevKey = prevKey,
            nextKey = nextKey
        )
    } catch (t: Throwable) {
        LoadResult.Error(t)
    }

    override fun getRefreshKey(state: PagingState<Long, T>): Long? {
        // A standard implementation for getRefreshKey
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(pageSize) ?: anchorPage?.nextKey?.minus(pageSize)
        }
    }
}