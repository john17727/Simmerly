package dev.juanrincon.simmerly.mealplan.data

import arrow.core.Either
import arrow.core.raise.either
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.domain.ResettableCache
import dev.juanrincon.simmerly.mealplan.data.local.MealPlanEntryDao
import dev.juanrincon.simmerly.mealplan.data.remote.MealPlanNetworkClient
import dev.juanrincon.simmerly.mealplan.domain.MealPlanDraft
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanError
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import dev.juanrincon.simmerly.mealplan.domain.PlanRule
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Offline-first meal plan: Room is the source of truth, the network fills it (as the timeline). */
class DefaultMealPlanRepository(
    private val networkClient: MealPlanNetworkClient,
    private val dao: MealPlanEntryDao,
    private val sessionDataStore: SessionDataStore,
    private val householdPrefs: HouseholdPreferencesStore,
    private val freshness: Freshness,
) : MealPlanRepository, ResettableCache {

    private var cachedRules: List<PlanRule>? = null

    override fun reset() {
        cachedRules = null
    }

    override fun observe(
        start: LocalDate,
        end: LocalDate,
    ): Flow<Either<MealPlanError, LoadingResult<List<MealPlanEntry>>>> = channelFlow {
        val key = CacheKeys.mealPlan(start.toString(), end.toString())
        val stale = freshness.isStale(key, CacheTtl.MEAL_PLAN)
        if (stale && dao.countRange(start.toString(), end.toString()) == 0) {
            send(Either.Right(LoadingResult.Loading))
            if (refresh(start, end).isLeft()) send(Either.Left(MealPlanError.FetchError))
        } else if (stale) {
            launch {
                send(Either.Right(LoadingResult.Refreshing))
                refresh(start, end).onLeft { send(Either.Left(it)) }
                send(Either.Right(LoadingResult.RefreshComplete))
            }
        }
        combine(
            sessionDataStore.observeServerAddress(),
            dao.observeRange(start.toString(), end.toString()),
        ) { host, rows -> rows.map { it.toDomain(host) } }
            .distinctUntilChanged()
            .collect { send(Either.Right(LoadingResult.Loaded(it))) }
    }

    override suspend fun refresh(start: LocalDate, end: LocalDate): Either<MealPlanError, Unit> = either {
        val page = networkClient.getEntries(start, end).mapLeft { MealPlanError.FetchError }.bind()
        val entities = page.items.map { it.toEntity() }
        dao.upsertAll(entities)
        if (entities.isEmpty()) {
            dao.deleteRange(start.toString(), end.toString())
        } else {
            dao.deleteMissingInRange(start.toString(), end.toString(), entities.map { it.id })
        }
        freshness.markSynced(CacheKeys.mealPlan(start.toString(), end.toString()))
    }

    override suspend fun add(draft: MealPlanDraft): Either<MealPlanError, MealPlanEntry> = either {
        val created = networkClient.create(draft.toDto()).mapLeft { MealPlanError.SaveError }.bind()
        dao.upsertAll(listOf(created.toEntity()))
        created.toEntity().toDomain(sessionDataStore.getServerAddress())
    }

    override suspend fun update(entry: MealPlanEntry): Either<MealPlanError, MealPlanEntry> = either {
        val updated = networkClient.update(entry.id, entry.toUpdateDto())
            .mapLeft { MealPlanError.SaveError }.bind()
        dao.upsertAll(listOf(updated.toEntity()))
        updated.toEntity().toDomain(sessionDataStore.getServerAddress())
    }

    override suspend fun remove(id: Int): Either<MealPlanError, Unit> = either {
        networkClient.delete(id).mapLeft { MealPlanError.DeleteError }.bind()
        dao.deleteById(id)
    }

    override suspend fun entry(id: Int): MealPlanEntry? =
        dao.getById(id)?.toDomain(sessionDataStore.getServerAddress())

    override fun observePlannedDates(): Flow<Map<String, List<LocalDate>>> =
        dao.observeRecipeDates().map { rows ->
            rows.groupBy({ it.recipeId }, { LocalDate.parse(it.date) }).mapValues { it.value.distinct() }
        }

    override fun observeFirstDayOfWeek(): Flow<DayOfWeek> = householdPrefs.observeFirstDayOfWeek()

    override suspend fun refreshFirstDayOfWeek() {
        networkClient.getPreferences().onRight { householdPrefs.setFirstDayOfWeek(it.firstDayOfWeek) }
    }

    override suspend fun rules(): List<PlanRule> {
        cachedRules?.let { return it }
        return networkClient.getRules().map { page -> page.items.map { it.toDomain() } }
            .onRight { cachedRules = it }
            .getOrNull().orEmpty()
    }
}
