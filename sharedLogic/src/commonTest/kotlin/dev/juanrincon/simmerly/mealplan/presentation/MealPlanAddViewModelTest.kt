package dev.juanrincon.simmerly.mealplan.presentation

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.mealplan.FakeMealPlanRepository
import dev.juanrincon.simmerly.mealplan.aPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.domain.PlanRule
import dev.juanrincon.simmerly.mealplan.presentation.orbit.AddTab
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.PlannedMeta
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MealPlanAddViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeMealPlanRepository
    private lateinit var recipes: FakeRecipeRepository

    private val today = LocalDate(2026, 11, 26)
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-11-26T12:00:00Z")
    }

    private fun summary(id: String, name: String, tags: List<Tag> = emptyList()) = RecipeSummary(
        id = id, name = name, image = "", tags = tags, rating = null, totalTime = null,
        prepTime = null, performTime = null, description = "",
    )

    private val carbonara = summary("r1", "Carbonara", listOf(Tag("t1", "g", "Pasta")))
    private val soup = summary("r2", "Tomato soup")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeMealPlanRepository()
        recipes = FakeRecipeRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm(args: MealPlanAddArgs = MealPlanAddArgs(date = today)) =
        MealPlanAddViewModel(args, repo, recipes, clock, TimeZone.UTC)

    private suspend fun emitRecipes() = recipes.allRecipesFlow.emit(listOf(carbonara, soup))

    @Test
    fun startsOnTheGivenDateAndMeal() {
        val state = vm(MealPlanAddArgs(date = today, type = MealType.Lunch)).container.stateFlow.value
        assertThat(state.date).isEqualTo(today)
        assertThat(state.type).isEqualTo(MealType.Lunch)
        assertThat(state.isEdit).isFalse()
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun searchAndChipsNarrowTheRecipeList() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            awaitStateMatching { it.allRecipes.size == 2 }

            viewModel.onEvent(MealPlanAddIntent.SetQuery("soup"))
            assertThat(awaitStateMatching { it.query == "soup" }.recipes.map { it.recipe.id }).isEqualTo(listOf("r2"))

            viewModel.onEvent(MealPlanAddIntent.SetQuery(""))
            viewModel.onEvent(MealPlanAddIntent.ToggleFilter("t1"))
            assertThat(awaitStateMatching { "t1" in it.selectedFilterIds }.recipes.map { it.recipe.id }).isEqualTo(listOf("r1"))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun theMetaShowsPlannedDateThisWeekOrNotPlanned() = runTest(dispatcher) {
        repo.plannedDates.value = mapOf("r1" to listOf(LocalDate(2026, 11, 28)))
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            repo.planFlow.emit(
                Either.Right(
                    LoadingResult.Loaded(
                        listOf(aPlanEntry(id = 5, date = LocalDate(2026, 11, 23), endDate = LocalDate(2026, 11, 29), recipeId = "r2"))
                    )
                )
            )
            val state = awaitStateMatching { it.allRecipes.size == 2 && it.weekEntries.isNotEmpty() && it.plannedDates.isNotEmpty() }
            val meta = state.recipes.associate { it.recipe.id to it.meta }
            assertThat(meta["r1"]).isEqualTo(PlannedMeta.On(LocalDate(2026, 11, 28)))
            assertThat(meta["r2"]).isEqualTo(PlannedMeta.ThisWeek)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aRecipeNeverPlannedIsNotPlannedYet() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            val state = awaitStateMatching { it.allRecipes.size == 2 }
            assertThat(state.recipes.first().meta).isEqualTo(PlannedMeta.NotPlanned)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun pickingARecipeShowsItsServingsAndEnablesSave() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            viewModel.onEvent(MealPlanAddIntent.SelectRecipe("r1"))
            recipes.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "r1", servings = 4.0))))

            val state = awaitStateMatching { it.servings != null }
            assertThat(state.servings).isEqualTo(4.0)
            assertThat(state.canSave).isTrue()

            viewModel.onEvent(MealPlanAddIntent.ChangeServings(+2))
            assertThat(awaitStateMatching { it.servings == 6.0 }.servings).isEqualTo(6.0)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun savingARecipeAddsAnEntryWithTheServingsScale() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            viewModel.onEvent(MealPlanAddIntent.SelectRecipe("r1"))
            recipes.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "r1", servings = 4.0))))
            awaitStateMatching { it.servings != null }
            viewModel.onEvent(MealPlanAddIntent.ChangeServings(+4))
            awaitStateMatching { it.servings == 8.0 }
            viewModel.onEvent(MealPlanAddIntent.Save)

            assertThat(awaitSideEffectSkippingStates()).isInstanceOf(MealPlanAddSideEffect.Saved::class)
            cancelAndIgnoreRemainingItems()
        }
        val draft = repo.added.single()
        assertThat(draft.recipeId).isEqualTo("r1")
        assertThat(draft.recipeScale).isEqualTo(2.0)
        assertThat(draft.date).isEqualTo(today)
        assertThat(draft.endDate).isNull()
    }

    @Test
    fun anyDayThisWeekSavesAWholeWeekEntry() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanAddIntent.SetTab(AddTab.Note))
            viewModel.onEvent(MealPlanAddIntent.SetNoteTitle("Leftovers"))
            viewModel.onEvent(MealPlanAddIntent.SetWholeWeek(true))
            awaitStateMatching { it.wholeWeek && it.noteTitle == "Leftovers" && it.tab == AddTab.Note }
            viewModel.onEvent(MealPlanAddIntent.Save)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        val draft = repo.added.single()
        assertThat(draft.date).isEqualTo(LocalDate(2026, 11, 23))
        assertThat(draft.endDate).isEqualTo(LocalDate(2026, 11, 29))
        assertThat(draft.recipeId).isNull()
        assertThat(draft.title).isEqualTo("Leftovers")
    }

    @Test
    fun aNoteNeedsATitle() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanAddIntent.SetTab(AddTab.Note))
            assertThat(awaitStateMatching { it.tab == AddTab.Note }.canSave).isFalse()
            viewModel.onEvent(MealPlanAddIntent.SetNoteTitle("  "))
            assertThat(awaitStateMatching { it.noteTitle == "  " }.canSave).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun warnsWhenTheRecipeIsAlreadyPlannedForThatMeal() = runTest(dispatcher) {
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            repo.planFlow.emit(Either.Right(LoadingResult.Loaded(listOf(aPlanEntry(id = 9, date = today, type = MealType.Dinner, recipeId = "r1")))))
            viewModel.onEvent(MealPlanAddIntent.SelectRecipe("r1"))
            assertThat(awaitStateMatching { it.selectedRecipeId == "r1" && it.weekEntries.isNotEmpty() }.isDuplicate).isTrue()

            viewModel.onEvent(MealPlanAddIntent.SetType(MealType.Lunch))
            assertThat(awaitStateMatching { it.type == MealType.Lunch }.isDuplicate).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun anAnyDayEntryOfThatMealCountsForAnyDayOfTheWeek() = runTest(dispatcher) {
        val viewModel = vm(MealPlanAddArgs(date = LocalDate(2026, 11, 25)))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipes()
            repo.planFlow.emit(
                Either.Right(
                    LoadingResult.Loaded(
                        listOf(aPlanEntry(id = 9, date = LocalDate(2026, 11, 23), endDate = LocalDate(2026, 11, 29), type = MealType.Dinner, recipeId = "r1"))
                    )
                )
            )
            viewModel.onEvent(MealPlanAddIntent.SelectRecipe("r1"))
            assertThat(awaitStateMatching { it.selectedRecipeId == "r1" && it.weekEntries.isNotEmpty() }.isDuplicate).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aRuleForTheMealEnablesThePlannerToggle() = runTest(dispatcher) {
        repo.rules = listOf(PlanRule(DayOfWeek.THURSDAY, MealType.Dinner, "tags.name = \"quick\""))
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            assertThat(awaitStateMatching { it.rulesAvailable }.rulesAvailable).isTrue()
            viewModel.onEvent(MealPlanAddIntent.SetRulesEnabled(true))
            assertThat(awaitStateMatching { it.rulesEnabled }.rulesEnabled).isTrue()

            viewModel.onEvent(MealPlanAddIntent.SetType(MealType.Lunch))
            val state = awaitStateMatching { it.type == MealType.Lunch && !it.rulesAvailable }
            assertThat(state.rulesEnabled).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun editingFillsInTheEntryAndSavesAnUpdate() = runTest(dispatcher) {
        repo.storedEntries[7] = aPlanEntry(id = 7, date = LocalDate(2026, 11, 27), type = MealType.Lunch, recipeId = "r1", scale = 2.0)
        val viewModel = vm(MealPlanAddArgs(date = today, entryId = 7))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            recipes.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "r1", servings = 4.0))))
            val state = awaitStateMatching { !it.isLoadingEntry && it.servings != null }
            assertThat(state.isEdit).isTrue()
            assertThat(state.date).isEqualTo(LocalDate(2026, 11, 27))
            assertThat(state.type).isEqualTo(MealType.Lunch)
            assertThat(state.servings).isEqualTo(8.0)

            viewModel.onEvent(MealPlanAddIntent.SetType(MealType.Dinner))
            awaitStateMatching { it.type == MealType.Dinner }
            viewModel.onEvent(MealPlanAddIntent.Save)
            val effect = awaitSideEffectSkippingStates() as MealPlanAddSideEffect.Saved
            assertThat(effect.wasEdit).isTrue()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.added).hasSize(0)
        val updated: MealPlanEntry = repo.updated.single()
        assertThat(updated.id).isEqualTo(7)
        assertThat(updated.type).isEqualTo(MealType.Dinner)
        assertThat(updated.recipeScale).isEqualTo(2.0)
    }

    @Test
    fun turningOffAnyDayOnAWeekEntryMovesItToTheViewedDay() = runTest(dispatcher) {
        repo.storedEntries[7] = aPlanEntry(id = 7, date = LocalDate(2026, 11, 23), endDate = LocalDate(2026, 11, 29), recipeId = null, title = "Leftovers")
        val viewModel = vm(MealPlanAddArgs(date = LocalDate(2026, 11, 25), entryId = 7))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            assertThat(awaitStateMatching { !it.isLoadingEntry && it.wholeWeek }.date).isEqualTo(LocalDate(2026, 11, 25))
            viewModel.onEvent(MealPlanAddIntent.SetWholeWeek(false))
            awaitStateMatching { !it.wholeWeek }
            viewModel.onEvent(MealPlanAddIntent.Save)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        val updated = repo.updated.single()
        assertThat(updated.date).isEqualTo(LocalDate(2026, 11, 25))
        assertThat(updated.endDate).isNull()
    }

    @Test
    fun openedFromARecipeTheRecipeIsFixed() = runTest(dispatcher) {
        val viewModel = vm(MealPlanAddArgs(date = today, recipeId = "r1"))
        viewModel.testWithInternalState(this) {
            runOnCreate()
            recipes.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loaded(aRecipeDetail(id = "r1", servings = 4.0))))
            val state = awaitStateMatching { it.servings != null }
            assertThat(state.recipeLocked).isTrue()
            assertThat(state.canSave).isTrue()
            viewModel.onEvent(MealPlanAddIntent.SelectRecipe("r2"))
            assertThat(viewModel.container.stateFlow.value.selectedRecipeId).isEqualTo("r1")
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aFailedSaveShowsAMessageAndStaysOpen() = runTest(dispatcher) {
        repo.saveFails = true
        val viewModel = vm()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            viewModel.onEvent(MealPlanAddIntent.SetTab(AddTab.Note))
            viewModel.onEvent(MealPlanAddIntent.SetNoteTitle("Fruit"))
            awaitStateMatching { it.noteTitle == "Fruit" && it.tab == AddTab.Note }
            viewModel.onEvent(MealPlanAddIntent.Save)
            assertThat(awaitSideEffectSkippingStates()).isInstanceOf(MealPlanAddSideEffect.ShowMessage::class)
            assertThat(viewModel.container.stateFlow.value.isSaving).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }
}
