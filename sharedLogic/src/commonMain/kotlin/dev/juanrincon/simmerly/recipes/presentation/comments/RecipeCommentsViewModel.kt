package dev.juanrincon.simmerly.recipes.presentation.comments

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsIntent
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsState
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toCommentUi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeCommentsViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
) : OrbitContainerHost<RecipeCommentsState, RecipeCommentsState, RecipeCommentsSideEffect>,
    ViewModel() {

    override val container: OrbitContainer<RecipeCommentsState, RecipeCommentsState, RecipeCommentsSideEffect> =
        orbitContainer(initialState = RecipeCommentsState()) {
            observeComments()
        }

    fun onEvent(event: RecipeCommentsIntent) {
        when (event) {
            is RecipeCommentsIntent.OnCommentTextChanged -> intent {
                reduce { state.copy(commentText = event.text) }
            }

            RecipeCommentsIntent.OnSendCommentClicked -> sendComment()
        }
    }

    private fun observeComments() = intent {
        repository.comments(recipeId)
            .distinctUntilChanged()
            .map { comments -> comments.map { it.toCommentUi() } }
            .collect { comments ->
                reduce { state.copy(comments = comments) }
            }
    }

    private fun sendComment() = intent {
        repository.addComment(recipeId, state.commentText).fold(
            ifRight = { reduce { state.copy(commentText = "") } },
            ifLeft = { /* TODO: handle error */ }
        )
    }

}
