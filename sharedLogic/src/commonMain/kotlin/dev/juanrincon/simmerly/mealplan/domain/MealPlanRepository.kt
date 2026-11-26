package dev.juanrincon.simmerly.mealplan.domain

import arrow.core.Either
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

interface MealPlanRepository {
    /** Entries touching [start]..[end] (incl. whole-week ones). Room first, refreshed when stale. */
    fun observe(
        start: LocalDate,
        end: LocalDate,
    ): Flow<Either<MealPlanError, LoadingResult<List<MealPlanEntry>>>>

    /** Explicit refresh (pull-to-refresh / retry): skips the TTL. */
    suspend fun refresh(start: LocalDate, end: LocalDate): Either<MealPlanError, Unit>

    suspend fun add(draft: MealPlanDraft): Either<MealPlanError, MealPlanEntry>

    /** PUT with the full body; used for edit, Cook (move to a day), servings and undo-move. */
    suspend fun update(entry: MealPlanEntry): Either<MealPlanError, MealPlanEntry>

    suspend fun remove(id: Int): Either<MealPlanError, Unit>

    /** One cached entry, for the edit screen. */
    suspend fun entry(id: Int): MealPlanEntry?

    /** Single-day plan dates per recipe id, from the plans saved on this device. Never hits the network. */
    fun observePlannedDates(): Flow<Map<String, List<LocalDate>>>

    /** Household first day of the week (cached; Monday until known). */
    fun observeFirstDayOfWeek(): Flow<DayOfWeek>

    suspend fun refreshFirstDayOfWeek()

    /** Household planner rules, cached after the first successful fetch. Empty when offline and uncached. */
    suspend fun rules(): List<PlanRule>
}
