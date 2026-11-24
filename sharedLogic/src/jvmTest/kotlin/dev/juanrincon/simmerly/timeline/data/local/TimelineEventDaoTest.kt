package dev.juanrincon.simmerly.timeline.data.local

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.core.aUserEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TimelineEventDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: TimelineEventDao

    private fun entity(id: String, recipe: String = "r1", user: String = "user-1", ts: String = "2026-11-21T10:00:00Z") =
        TimelineEventEntity(id, recipe, user, "subject $id", "comment", "msg", false, Instant.parse(ts))

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.timelineEventDao()
        runBlocking { db.userDao().upsert(aUserEntity("user-1", fullName = "Juan Rincon")) }
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeReturnsOnlyThatRecipeNewestFirstWithItsAuthor() = runTest {
        dao.upsertAll(
            listOf(
                entity("old", ts = "2026-11-01T10:00:00Z"),
                entity("new", ts = "2026-11-20T10:00:00Z"),
                entity("other", recipe = "r2"),
            )
        )

        dao.observe("r1").test {
            val rows = awaitItem()
            assertThat(rows.map { it.event.id }).isEqualTo(listOf("new", "old"))
            assertThat(rows.first().user?.fullName).isEqualTo("Juan Rincon")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun anAuthorMissingFromTheUsersTableStillLoadsTheEvent() = runTest {
        dao.upsertAll(listOf(entity("e1", user = "stranger")))

        dao.observe("r1").test {
            assertThat(awaitItem().single().user).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsertReplacesAnExistingRow() = runTest {
        dao.upsertAll(listOf(entity("e1")))
        dao.upsertAll(listOf(entity("e1").copy(subject = "edited", hasImage = true)))

        dao.observe("r1").test {
            val row = awaitItem().single().event
            assertThat(row.subject).isEqualTo("edited")
            assertThat(row.hasImage).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMissingKeepsOnlyTheListedIdsOfThatRecipe() = runTest {
        dao.upsertAll(listOf(entity("a"), entity("b"), entity("c"), entity("x", recipe = "r2")))

        dao.deleteMissing("r1", listOf("a", "c"))

        assertThat(dao.count("r1")).isEqualTo(2)
        assertThat(dao.count("r2")).isEqualTo(1)
    }

    @Test
    fun deleteByIdAndByRecipeRemoveRows() = runTest {
        dao.upsertAll(listOf(entity("a"), entity("b"), entity("x", recipe = "r2")))

        dao.deleteById("a")
        assertThat(dao.count("r1")).isEqualTo(1)

        dao.deleteByRecipeId("r1")
        assertThat(dao.count("r1")).isEqualTo(0)
        dao.observe("r2").test {
            assertThat(awaitItem()).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearingAllTablesOnLogoutAlsoClearsTheTimeline() = runTest {
        dao.upsertAll(listOf(entity("a")))

        db.clearAllTables()

        assertThat(dao.count("r1")).isEqualTo(0)
    }
}
