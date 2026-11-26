package dev.juanrincon.simmerly.mealplan.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.mealplan.domain.MealPlanDraft
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import dev.juanrincon.simmerly.mealplan.domain.PlannedRecipe
import dev.juanrincon.simmerly.mealplan.presentation.orbit.AddTab
import dev.juanrincon.simmerly.mealplan.presentation.orbit.FilterChipUi
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddState
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Add and edit share this one screen: edit just starts with the entry's values filled in. */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class MealPlanAddViewModel(
    private val args: MealPlanAddArgs,
    private val repository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    clock: Clock = Clock.System,
    zone: TimeZone = TimeZone.currentSystemDefault(),
) : OrbitContainerHost<MealPlanAddState, MealPlanAddState, MealPlanAddSideEffect>, ViewModel() {

    private val today = clock.todayIn(zone)
    private val view = MutableStateFlow(args.date to DayOfWeek.MONDAY)
    private var detailJob: Job? = null

    /** Scale of the entry being edited until the recipe's base servings are known. */
    private var pendingScale: Double? = null

    override val container: OrbitContainer<MealPlanAddState, MealPlanAddState, MealPlanAddSideEffect> =
        orbitContainer(
            initialState = MealPlanAddState(
                today = today,
                date = args.date,
                type = args.type,
                editingId = args.entryId,
                recipeLocked = args.recipeId != null,
                isLoadingEntry = args.entryId != null,
                selectedRecipeId = args.recipeId,
            )
        ) {
            observeFirstDay()
            observeRecipes()
            observePlannedDates()
            observeWeek()
            loadRule()
            args.entryId?.let(::loadEntry)
            args.recipeId?.let(::watchRecipe)
        }

    // Concrete, non-generic accessors for Swift.
    val stateFlow: StateFlow<MealPlanAddState> get() = container.stateFlow
    val sideEffectFlow: Flow<MealPlanAddSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: MealPlanAddIntent) {
        when (event) {
            is MealPlanAddIntent.SetDate -> setDate(event.date)
            is MealPlanAddIntent.SetWholeWeek -> intent { reduce { state.copy(wholeWeek = event.wholeWeek) } }
            is MealPlanAddIntent.SetType -> setType(event.type)
            is MealPlanAddIntent.SetTab -> intent { reduce { state.copy(tab = event.tab) } }
            is MealPlanAddIntent.SetQuery -> intent { reduce { state.copy(query = event.query) } }
            is MealPlanAddIntent.ToggleFilter -> intent {
                reduce {
                    val ids = if (event.id in state.selectedFilterIds) state.selectedFilterIds - event.id
                    else state.selectedFilterIds + event.id
                    state.copy(selectedFilterIds = ids)
                }
            }
            is MealPlanAddIntent.SetRulesEnabled -> intent { reduce { state.copy(rulesEnabled = event.enabled && state.rulesAvailable) } }
            is MealPlanAddIntent.SelectRecipe -> {
                if (container.stateFlow.value.recipeLocked) return
                intent { reduce { state.copy(selectedRecipeId = event.recipeId, baseServings = null, servings = null) } }
                watchRecipe(event.recipeId)
            }
            is MealPlanAddIntent.ChangeServings -> intent {
                val current = state.servings ?: return@intent
                reduce { state.copy(servings = (current + event.delta).coerceAtLeast(1.0)) }
            }
            is MealPlanAddIntent.SetNoteTitle -> intent { reduce { state.copy(noteTitle = event.title) } }
            is MealPlanAddIntent.SetNoteText -> intent { reduce { state.copy(noteText = event.text) } }
            MealPlanAddIntent.Save -> save()
        }
    }

    private fun setDate(date: LocalDate) {
        view.value = date to view.value.second
        intent { reduce { state.copy(date = date) } }
        loadRule()
    }

    private fun setType(type: dev.juanrincon.simmerly.mealplan.domain.MealType) {
        intent { reduce { state.copy(type = type) } }
        loadRule()
    }

    private fun observeFirstDay() = intent {
        repository.observeFirstDayOfWeek().collect { first ->
            view.value = view.value.first to first
            reduce { state.copy(firstDay = first) }
        }
    }

    private fun observeRecipes() = intent {
        recipeRepository.observeAllRecipes().collect { recipes ->
            val tags = recipes.flatMap { r -> r.tags.map { FilterChipUi(it.id, it.name, isTag = true) } }
            val categories = recipes.flatMap { r -> r.categories.map { FilterChipUi(it.id, it.name, isTag = false) } }
            reduce {
                state.copy(allRecipes = recipes, filters = (categories + tags).distinctBy { it.id }.sortedBy { it.name })
            }
        }
    }

    private fun observePlannedDates() = intent {
        repository.observePlannedDates().collect { reduce { state.copy(plannedDates = it) } }
    }

    private fun observeWeek() = intent {
        view.map { (date, first) -> date.startOfWeek(first) }
            .distinctUntilChanged()
            .flatMapLatest { start -> repository.observe(start, start.plusDays(6)) }
            .collect { result ->
                result.onRight { loading ->
                    if (loading is LoadingResult.Loaded) reduce { state.copy(weekEntries = loading.data) }
                }
            }
    }

    /** A rule applies when it matches the meal and the weekday (or is for every day). */
    private fun loadRule() = intent {
        val rules = repository.rules()
        val s = state
        val rule = rules.firstOrNull { it.type == s.type && it.day == s.date.dayOfWeek }
            ?: rules.firstOrNull { it.type == s.type && it.day == null }
        reduce { state.copy(rule = rule, rulesEnabled = state.rulesEnabled && rule != null) }
    }

    private fun loadEntry(id: Int) = intent {
        val entry = repository.entry(id)
        if (entry == null) {
            reduce { state.copy(isLoadingEntry = false) }
            return@intent
        }
        pendingScale = entry.recipeScale
        reduce {
            state.copy(
                isLoadingEntry = false,
                // A week entry opens on the day the user was viewing; turning "any day" off moves it there.
                date = if (entry.isWholeWeek) args.date else entry.date,
                wholeWeek = entry.isWholeWeek,
                type = entry.type,
                tab = if (entry.isNote) AddTab.Note else AddTab.Recipe,
                selectedRecipeId = entry.recipe?.id,
                noteTitle = entry.title,
                noteText = entry.text,
            )
        }
        entry.recipe?.id?.let(::watchRecipe)
        loadRule()
    }

    /** Base servings come from the recipe itself; the stepper shows base x scale. */
    private fun watchRecipe(id: String) {
        detailJob?.cancel()
        detailJob = intent {
            recipeRepository.recipeDetails(id).collect { result ->
                result.onRight { loading ->
                    if (loading is LoadingResult.Loaded) {
                        val base = loading.data.servings
                        reduce {
                            if (state.selectedRecipeId != id) state
                            else state.copy(
                                baseServings = base,
                                servings = state.servings ?: (base * (pendingScale ?: 1.0)),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun save() = intent {
        val s = state
        if (!s.canSave) return@intent
        reduce { state.copy(isSaving = true) }
        val start = s.date.startOfWeek(s.firstDay)
        val date = if (s.wholeWeek) start else s.date
        val endDate = if (s.wholeWeek) start.plusDays(6) else null
        val recipe = s.takeIf { it.tab == AddTab.Recipe }?.selectedRecipeId?.let { id ->
            val summary = s.allRecipes.firstOrNull { it.id == id }
            PlannedRecipe(id, summary?.name.orEmpty(), s.baseServings ?: 1.0, summary?.image.orEmpty())
        }
        val scale = if (recipe != null && s.baseServings != null && s.servings != null) {
            s.servings / s.baseServings
        } else 1.0
        val title = if (recipe == null) s.noteTitle.trim() else ""
        val text = if (recipe == null) s.noteText.trim() else ""

        val result = if (s.editingId != null) {
            val existing = repository.entry(s.editingId)
            if (existing == null) null
            else repository.update(
                existing.copy(
                    date = date, endDate = endDate, type = s.type, title = title, text = text,
                    recipe = recipe, recipeScale = scale,
                )
            )
        } else {
            repository.add(
                MealPlanDraft(
                    date = date, endDate = endDate, type = s.type, title = title, text = text,
                    recipeId = recipe?.id, recipeScale = scale,
                )
            )
        }
        reduce { state.copy(isSaving = false) }
        result?.fold(
            ifLeft = {
                postSideEffect(MealPlanAddSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
            },
            ifRight = { postSideEffect(MealPlanAddSideEffect.Saved(it, wasEdit = s.editingId != null)) },
        ) ?: postSideEffect(MealPlanAddSideEffect.ShowMessage(UiText.Resource(StringKey.MealPlanSaveFailed)))
    }
}
