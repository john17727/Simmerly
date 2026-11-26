package dev.juanrincon.simmerly.mealplan.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.mealplan.data.local.MealPlanEntryEntity
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanRuleDto
import dev.juanrincon.simmerly.mealplan.domain.MealType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class MealPlanMappersTest {

    private fun entity(recipeId: String? = "r1", end: String? = null, type: String = "dinner") = MealPlanEntryEntity(
        id = 3, groupId = "g", userId = "u", date = "2026-11-26", endDate = end, entryType = type, title = "",
        text = "", recipeId = recipeId, recipeScale = 1.5, recipeName = "Carbonara", recipeServings = 4.0,
    )

    @Test
    fun anEntityBecomesADomainEntryWithServingsAndImageUrl() {
        val entry = entity().toDomain("https://m.example")

        assertThat(entry.date).isEqualTo(LocalDate(2026, 11, 26))
        assertThat(entry.recipe!!.name).isEqualTo("Carbonara")
        assertThat(entry.recipe!!.image).isEqualTo("https://m.example/api/media/recipes/r1/images/min-original.webp")
        assertThat(entry.servings).isEqualTo(6.0)
        assertThat(entry.isWholeWeek).isEqualTo(false)
    }

    @Test
    fun anEntryWithAnEndDateIsAWholeWeekEntryAndCoversEveryDayOfIt() {
        val entry = entity(end = "2026-11-29").toDomain(null)

        assertThat(entry.isWholeWeek).isEqualTo(true)
        assertThat(entry.coversDay(LocalDate(2026, 11, 28))).isEqualTo(true)
        assertThat(entry.coversDay(LocalDate(2026, 11, 30))).isEqualTo(false)
    }

    @Test
    fun aNoteHasNoRecipeOrServings() {
        val entry = entity(recipeId = null).toDomain(null)

        assertThat(entry.isNote).isEqualTo(true)
        assertThat(entry.servings).isNull()
    }

    @Test
    fun unknownMealTypesFallBackToDinner() {
        assertThat(MealType.fromApi("brunch")).isEqualTo(MealType.Dinner)
        assertThat(MealType.fromApi("dessert")).isEqualTo(MealType.Dessert)
    }

    @Test
    fun updateDtoCarriesTheIdentifiers() {
        val dto = entity().toDomain(null).toUpdateDto()

        assertThat(dto.id).isEqualTo(3)
        assertThat(dto.groupId).isEqualTo("g")
        assertThat(dto.userId).isEqualTo("u")
        assertThat(dto.recipeScale).isEqualTo(1.5)
    }

    @Test
    fun rulesMapDaysAndUnsetMeansEveryDay() {
        assertThat(PlanRuleDto("1", "friday", "lunch", "f").toDomain().day).isEqualTo(DayOfWeek.FRIDAY)
        assertThat(PlanRuleDto("1", "unset", "lunch", "f").toDomain().day).isNull()
    }

    @Test
    fun householdFirstDayNumbersMapSundayFirst() {
        assertThat(0.toDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY)
        assertThat(1.toDayOfWeek()).isEqualTo(DayOfWeek.MONDAY)
        assertThat(6.toDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY)
    }
}
