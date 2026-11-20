package dev.juanrincon.simmerly.recipes.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.auth.FakeSessionDataStore
import dev.juanrincon.simmerly.core.aRecipeEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RecipePagingSourceTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var source: RecipePagingSource

    @BeforeTest
    fun setUp() = runTest {
        db = buildTestDatabase()
        val base = Instant.fromEpochSeconds(1_700_000_000)
        repeat(220) { db.recipeDao().upsert(aRecipeEntity(id = "r$it", createdAt = base + it.minutes)) }
        source = RecipePagingSource(db.recipeDao(), db.userRecipePreferenceDao(), FakeSessionDataStore())
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private fun page(key: Int?, loadSize: Int, prepend: Boolean = false): LoadParams<Int> =
        if (prepend) LoadParams.Prepend(key!!, loadSize, false)
        else if (key == null) LoadParams.Refresh(null, loadSize, false)
        else LoadParams.Append(key, loadSize, false)

    @Test
    fun firstRefreshHasNoPrevKey() = runTest {
        val result = source.load(page(null, 150)) as LoadResult.Page

        assertThat(result.data).hasSize(150)
        assertThat(result.prevKey).isNull()
        assertThat(result.nextKey).isEqualTo(150)
    }

    @Test
    fun refreshInTheMiddleHasPageSizedPrevKeyAndNeverNegative() = runTest {
        val refresh = source.load(LoadParams.Refresh(100, 150, false)) as LoadResult.Page
        assertThat(refresh.prevKey).isEqualTo(50)

        val prepend = source.load(page(50, 50, prepend = true)) as LoadResult.Page
        assertThat(prepend.prevKey).isEqualTo(0)
        // Newest first, so row 99 is r120: it sits directly above the page that started at row 100.
        assertThat(prepend.data).hasSize(50)
        assertThat(prepend.data.last().id).isEqualTo("r120")
        assertThat(refresh.data.first().id).isEqualTo("r119")
    }

    @Test
    fun lastPageHasNoNextKey() = runTest {
        val result = source.load(page(200, 50)) as LoadResult.Page

        assertThat(result.data).hasSize(20)
        assertThat(result.nextKey).isNull()
    }

    @Test
    fun refreshKeyAccountsForTheFirstLoadedPage() {
        val config = PagingConfig(pageSize = 50, enablePlaceholders = false)
        fun pageAt(start: Int) = LoadResult.Page<Int, RecipeSummary>(
            data = emptyList(),
            prevKey = if (start == 0) null else start - 50,
            nextKey = start + 150,
        )

        // Anchor at item 10 of a list whose first page starts at row 100 → absolute row 110.
        val state = PagingState(listOf(pageAt(100)), anchorPosition = 10, config = config, leadingPlaceholderCount = 0)
        // 110 - initialLoadSize/2 (75) = 35, rounded down to a page boundary.
        assertThat(source.getRefreshKey(state)).isEqualTo(0)

        val deep = PagingState(listOf(pageAt(100)), anchorPosition = 140, config = config, leadingPlaceholderCount = 0)
        // 240 - 75 = 165 → 150.
        assertThat(source.getRefreshKey(deep)).isEqualTo(150)

        val noAnchor = PagingState(listOf(pageAt(0)), anchorPosition = null, config = config, leadingPlaceholderCount = 0)
        assertThat(source.getRefreshKey(noAnchor)).isNull()
    }
}
