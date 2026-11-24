package dev.juanrincon.simmerly.timeline.presentation

import arrow.core.Either
import arrow.core.left
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.recipes.FakeRecipeRepository
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.shoppinglists.awaitSideEffectSkippingStates
import dev.juanrincon.simmerly.shoppinglists.awaitStateMatching
import dev.juanrincon.simmerly.timeline.FakeTimelineRepository
import dev.juanrincon.simmerly.timeline.aTimelineEvent
import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.domain.TimelineError
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TimelineEditViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeTimelineRepository
    private lateinit var recipes: FakeRecipeRepository
    private lateinit var viewModel: TimelineEditViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeTimelineRepository()
        recipes = FakeRecipeRepository()
        viewModel = TimelineEditViewModel("recipe-1", "e1", repo, recipes)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun emitRecipeAndEvents(vararg events: dev.juanrincon.simmerly.timeline.domain.TimelineEvent) {
        recipes.recipeDetailsFlow().emit(Either.Right(LoadingResult.Loaded(aRecipeDetail().copy(name = "Carbonara"))))
        repo.timelineFlow.emit(Either.Right(LoadingResult.Loaded(events.toList())))
    }

    @Test
    fun theEntryIsLoadedIntoTheDraft() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1", subject = "Made it", message = "Yum", image = "http://x/p"))

            val state = awaitStateMatching { it.isLoaded }
            assertThat(state.subject).isEqualTo("Made it")
            assertThat(state.message).isEqualTo("Yum")
            assertThat(state.recipeName).isEqualTo("Carbonara")
            assertThat(state.hasPhoto).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun anEntryTheUserCannotEditClosesTheScreen() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1", canEdit = false))

            assertThat(awaitSideEffectSkippingStates()).isEqualTo(TimelineEditSideEffect.Finished)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun aMissingEntryClosesTheScreen() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("other"))

            assertThat(awaitSideEffectSkippingStates()).isEqualTo(TimelineEditSideEffect.Finished)
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun saveSendsTheTrimmedDraftAndFinishes() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1"))
            awaitStateMatching { it.isLoaded }
            viewModel.onEvent(TimelineEditIntent.SubjectChanged("  New subject "))
            viewModel.onEvent(TimelineEditIntent.MessageChanged("   "))
            awaitStateMatching { it.subject == "  New subject " && it.message == "   " }

            viewModel.onEvent(TimelineEditIntent.Save)

            assertThat(awaitSideEffectSkippingStates()).isEqualTo(TimelineEditSideEffect.Finished)
            cancelAndIgnoreRemainingItems()
        }
        val call = repo.lastUpdate!!
        assertThat(call.subject).isEqualTo("New subject")
        assertThat(call.message).isNull()
    }

    @Test
    fun aBlankSubjectCannotBeSaved() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1"))
            awaitStateMatching { it.isLoaded }
            viewModel.onEvent(TimelineEditIntent.SubjectChanged(" "))
            val state = awaitStateMatching { it.subject == " " }
            assertThat(state.canSave).isFalse()

            viewModel.onEvent(TimelineEditIntent.Save)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.lastUpdate).isNull()
    }

    @Test
    fun aFailedSaveStaysOnTheScreenWithAMessage() = runTest(dispatcher) {
        repo.updateResult = TimelineError.UpdateError.left()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1"))
            awaitStateMatching { it.isLoaded }

            viewModel.onEvent(TimelineEditIntent.Save)

            assertThat(awaitSideEffectSkippingStates() is TimelineEditSideEffect.ShowMessage).isTrue()
            assertThat(viewModel.container.stateFlow.value.isSaving).isFalse()
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun pickingAPhotoReplacesTheExistingOneAndRemovingMarksItForDeletion() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1", image = "http://x/p"))
            awaitStateMatching { it.isLoaded }

            viewModel.onEvent(TimelineEditIntent.RemovePhoto)
            val removed = awaitStateMatching { it.removeImage }
            assertThat(removed.hasPhoto).isFalse()

            viewModel.onEvent(TimelineEditIntent.PhotoPicked(PickedImage(byteArrayOf(1), "jpg")))
            val picked = awaitStateMatching { it.pickedImage != null }
            assertThat(picked.hasPhoto).isTrue()
            assertThat(picked.removeImage).isFalse()

            viewModel.onEvent(TimelineEditIntent.Save)
            awaitSideEffectSkippingStates()
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.lastUpdate!!.newImage!!.extension).isEqualTo("jpg")
    }

    @Test
    fun deleteAsksFirstThenDeletesAndFinishes() = runTest(dispatcher) {
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1"))
            awaitStateMatching { it.isLoaded }

            viewModel.onEvent(TimelineEditIntent.DeleteClicked)
            awaitStateMatching { it.showDeleteConfirm }
            assertThat(repo.deletedIds.size).isEqualTo(0)

            viewModel.onEvent(TimelineEditIntent.DeleteConfirmed)
            assertThat(awaitSideEffectSkippingStates()).isEqualTo(TimelineEditSideEffect.Finished)
            cancelAndIgnoreRemainingItems()
        }
        assertThat(repo.deletedIds).isEqualTo(listOf("e1"))
    }

    @Test
    fun aFailedDeleteStaysOnTheScreenWithAMessage() = runTest(dispatcher) {
        repo.deleteResult = TimelineError.DeleteError.left()
        viewModel.testWithInternalState(this) {
            runOnCreate()
            emitRecipeAndEvents(aTimelineEvent("e1"))
            awaitStateMatching { it.isLoaded }

            viewModel.onEvent(TimelineEditIntent.DeleteConfirmed)

            assertThat(awaitSideEffectSkippingStates() is TimelineEditSideEffect.ShowMessage).isTrue()
            cancelAndIgnoreRemainingItems()
        }
    }
}
