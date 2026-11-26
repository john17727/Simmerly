package dev.juanrincon.simmerly.mealplan.data.local

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MealPlanEntryDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: MealPlanEntryDao

    private fun entity(id: Int, date: String, end: String? = null, recipe: String? = "r1") = MealPlanEntryEntity(
        id = id, groupId = "g", userId = "u", date = date, endDate = end, entryType = "dinner",
        title = "", text = "", recipeId = recipe, recipeScale = 1.0, recipeName = "Name", recipeServings = 4.0,
    )

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.mealPlanEntryDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeRangeReturnsEntriesInsideAndWeekEntriesOverlappingTheRange() = runTest {
        dao.upsertAll(
            listOf(
                entity(1, "2026-11-25"),
                entity(2, "2026-12-10"),
                entity(3, "2026-11-23", end = "2026-11-29"),
                entity(4, "2026-11-16", end = "2026-11-22"),
            )
        )

        dao.observeRange("2026-11-24", "2026-11-30").test {
            assertThat(awaitItem().map { it.id }).isEqualTo(listOf(3, 1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMissingInRangeKeepsListedAndOutOfRangeRows() = runTest {
        dao.upsertAll(listOf(entity(1, "2026-11-25"), entity(2, "2026-11-26"), entity(3, "2026-12-20")))

        dao.deleteMissingInRange("2026-11-23", "2026-11-29", listOf(1))

        dao.observeRange("2000-01-01", "2100-01-01").test {
            assertThat(awaitItem().map { it.id }).isEqualTo(listOf(1, 3))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun countRangeAndDeleteRange() = runTest {
        dao.upsertAll(listOf(entity(1, "2026-11-25"), entity(2, "2026-12-20")))
        assertThat(dao.countRange("2026-11-23", "2026-11-29")).isEqualTo(1)

        dao.deleteRange("2026-11-23", "2026-11-29")

        assertThat(dao.countRange("2026-11-23", "2026-11-29")).isEqualTo(0)
        assertThat(dao.getById(2)?.id).isEqualTo(2)
    }

    @Test
    fun recipeDatesSkipWeekEntriesAndNotes() = runTest {
        dao.upsertAll(
            listOf(
                entity(1, "2026-11-27", recipe = "r1"),
                entity(2, "2026-11-23", end = "2026-11-29", recipe = "r1"),
                entity(3, "2026-11-28", recipe = null),
            )
        )

        dao.observeRecipeDates().test {
            assertThat(awaitItem()).isEqualTo(listOf(RecipeDateRow("r1", "2026-11-27")))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
