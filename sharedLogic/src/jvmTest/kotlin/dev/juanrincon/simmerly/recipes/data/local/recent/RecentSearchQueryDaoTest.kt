package dev.juanrincon.simmerly.recipes.data.local.recent

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.aRecentSearchQueryEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RecentSearchQueryDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: RecentSearchQueryDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.recentSearchQueryDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // region upsert

    @Test
    fun upsertStoresQuery() = runTest {
        dao.observe().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecentSearchQueryEntity(query = "pasta"))
            assertThat(awaitItem().first().query).isEqualTo("pasta")
        }
    }

    @Test
    fun upsertWithSameQueryUpdatesSearchedAt() = runTest {
        dao.observe().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecentSearchQueryEntity(query = "pasta", searchedAt = 1000L))
            awaitItem()
            dao.upsert(aRecentSearchQueryEntity(query = "pasta", searchedAt = 2000L))
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().searchedAt).isEqualTo(2000L)
        }
    }

    // endregion

    // region observe ordering and limit

    @Test
    fun observeReturnsQueriesInDescendingOrderBySearchedAt() = runTest {
        // Write before subscribing — Room emits current state on first collection
        dao.upsert(aRecentSearchQueryEntity("early", searchedAt = 1000L))
        dao.upsert(aRecentSearchQueryEntity("middle", searchedAt = 2000L))
        dao.upsert(aRecentSearchQueryEntity("latest", searchedAt = 3000L))

        dao.observe().test {
            val list = awaitItem()
            assertThat(list[0].query).isEqualTo("latest")
            assertThat(list[1].query).isEqualTo("middle")
            assertThat(list[2].query).isEqualTo("early")
        }
    }

    @Test
    fun observeReturnsAtMostTenQueries() = runTest {
        repeat(15) { i ->
            dao.upsert(aRecentSearchQueryEntity(query = "query-$i", searchedAt = i.toLong()))
        }

        dao.observe().test {
            assertThat(awaitItem()).hasSize(10)
        }
    }

    @Test
    fun observeEmitsEmptyListInitially() = runTest {
        dao.observe().test {
            assertThat(awaitItem()).hasSize(0)
        }
    }

    @Test
    fun observeEmitsUpdatedListAfterInsert() = runTest {
        dao.observe().test {
            assertThat(awaitItem()).hasSize(0)
            dao.upsert(aRecentSearchQueryEntity("pasta"))
            assertThat(awaitItem()).hasSize(1)
        }
    }

    // endregion
}
