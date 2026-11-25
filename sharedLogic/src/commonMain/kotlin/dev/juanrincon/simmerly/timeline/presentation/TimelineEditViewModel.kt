package dev.juanrincon.simmerly.timeline.presentation

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.timeline.domain.TimelineRepository
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditSideEffect
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.ExperimentalTime

/** Edits one entry. Nothing is kept on leaving without saving, so the ViewModel is the draft. */
@OptIn(ExperimentalTime::class)
class TimelineEditViewModel(
    private val recipeId: String,
    private val eventId: String,
    private val repository: TimelineRepository,
    private val recipeRepository: RecipeRepository,
) : OrbitContainerHost<TimelineEditState, TimelineEditState, TimelineEditSideEffect>, ViewModel() {

    override val container: OrbitContainer<TimelineEditState, TimelineEditState, TimelineEditSideEffect> =
        orbitContainer(initialState = TimelineEditState()) { load() }

    val stateFlow: StateFlow<TimelineEditState> get() = container.stateFlow
    val sideEffectFlow: Flow<TimelineEditSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: TimelineEditIntent) {
        when (event) {
            is TimelineEditIntent.SubjectChanged -> intent { reduce { state.copy(subject = event.text) } }
            is TimelineEditIntent.MessageChanged -> intent { reduce { state.copy(message = event.text) } }
            is TimelineEditIntent.PhotoPicked -> intent {
                reduce { state.copy(pickedImage = event.image, removeImage = false) }
            }

            TimelineEditIntent.RemovePhoto -> intent {
                reduce { state.copy(pickedImage = null, removeImage = true) }
            }

            TimelineEditIntent.Save -> save()
            TimelineEditIntent.DeleteClicked -> intent { reduce { state.copy(showDeleteConfirm = true) } }
            TimelineEditIntent.DeleteDismissed -> intent { reduce { state.copy(showDeleteConfirm = false) } }
            TimelineEditIntent.DeleteConfirmed -> delete()
        }
    }

    private fun load() = intent {
        val recipeName = recipeRepository.recipeDetails(recipeId)
            .mapNotNull { it.getOrNull()?.loadedOrNull() }
            .first()
            .name
        val event = repository.timeline(recipeId)
            .mapNotNull { it.getOrNull()?.loadedOrNull() }
            .first()
            .firstOrNull { it.id == eventId }
        if (event == null || !event.canEdit) {
            postSideEffect(TimelineEditSideEffect.Finished)
            return@intent
        }
        reduce {
            state.copy(
                isLoaded = true,
                type = event.type,
                date = formatTimelineDate(event.timestamp),
                recipeName = recipeName,
                subject = event.subject,
                message = event.message.orEmpty(),
                existingImage = event.image,
            )
        }
    }

    private fun save() = intent {
        if (!state.canSave) return@intent
        reduce { state.copy(isSaving = true) }
        repository.updateEvent(
            recipeId = recipeId,
            eventId = eventId,
            subject = state.subject.trim(),
            message = state.message.trim().ifEmpty { null },
            newImage = state.pickedImage,
            removeImage = state.removeImage,
        ).fold(
            ifLeft = {
                reduce { state.copy(isSaving = false) }
                postSideEffect(TimelineEditSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineSaveFailed)))
            },
            ifRight = { postSideEffect(TimelineEditSideEffect.Finished) },
        )
    }

    private fun delete() = intent {
        if (state.isDeleting) return@intent
        reduce { state.copy(isDeleting = true, showDeleteConfirm = false) }
        repository.deleteEvent(recipeId, eventId).fold(
            ifLeft = {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(TimelineEditSideEffect.ShowMessage(UiText.Resource(StringKey.TimelineDeleteFailed)))
            },
            ifRight = { postSideEffect(TimelineEditSideEffect.Finished) },
        )
    }
}

private fun <T> LoadingResult<T>.loadedOrNull(): T? = (this as? LoadingResult.Loaded<T>)?.data
