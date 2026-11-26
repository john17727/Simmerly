package dev.juanrincon.simmerly.mealplan

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.juanrincon.simmerly.mealplan.domain.MealPlanDraft
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanError
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.domain.PlanRule
import dev.juanrincon.simmerly.mealplan.domain.PlannedRecipe
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

fun aPlanEntry(
    id: Int = 1,
    date: LocalDate = LocalDate(2026, 11, 26),
    endDate: LocalDate? = null,
    type: MealType = MealType.Dinner,
    recipeId: String? = "r1",
    recipeName: String = "Carbonara",
    recipeServings: Double = 4.0,
    scale: Double = 1.0,
    title: String = "",
) = MealPlanEntry(
    id = id, groupId = "g", userId = "u", date = date, endDate = endDate, type = type,
    title = title, text = "",
    recipe = recipeId?.let { PlannedRecipe(it, recipeName, recipeServings, "") },
    recipeScale = scale,
)

class FakeMealPlanRepository : MealPlanRepository {
    val planFlow = MutableSharedFlow<Either<MealPlanError, LoadingResult<List<MealPlanEntry>>>>(replay = 1)
    val firstDay = MutableStateFlow(DayOfWeek.MONDAY)
    val plannedDates = MutableStateFlow<Map<String, List<LocalDate>>>(emptyMap())

    var refreshResult: Either<MealPlanError, Unit> = Unit.right()
    var saveFails = false
    var rules: List<PlanRule> = emptyList()
    val storedEntries = mutableMapOf<Int, MealPlanEntry>()

    var refreshCount = 0
    val observedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()
    val added = mutableListOf<MealPlanDraft>()
    val updated = mutableListOf<MealPlanEntry>()
    val removedIds = mutableListOf<Int>()

    override fun observe(start: LocalDate, end: LocalDate): Flow<Either<MealPlanError, LoadingResult<List<MealPlanEntry>>>> {
        observedRanges += start to end
        return planFlow
    }

    override suspend fun refresh(start: LocalDate, end: LocalDate): Either<MealPlanError, Unit> {
        refreshCount++
        return refreshResult
    }

    override suspend fun add(draft: MealPlanDraft): Either<MealPlanError, MealPlanEntry> {
        if (saveFails) return MealPlanError.SaveError.left()
        added += draft
        return aPlanEntry(
            id = 100, date = draft.date, endDate = draft.endDate, type = draft.type,
            recipeId = draft.recipeId, scale = draft.recipeScale, title = draft.title,
        ).right()
    }

    override suspend fun update(entry: MealPlanEntry): Either<MealPlanError, MealPlanEntry> {
        if (saveFails) return MealPlanError.SaveError.left()
        updated += entry
        return entry.right()
    }

    override suspend fun remove(id: Int): Either<MealPlanError, Unit> {
        if (saveFails) return MealPlanError.DeleteError.left()
        removedIds += id
        return Unit.right()
    }

    override suspend fun entry(id: Int): MealPlanEntry? = storedEntries[id]

    override fun observePlannedDates(): Flow<Map<String, List<LocalDate>>> = plannedDates

    override fun observeFirstDayOfWeek(): Flow<DayOfWeek> = firstDay

    override suspend fun refreshFirstDayOfWeek() = Unit

    override suspend fun rules(): List<PlanRule> = rules
}
