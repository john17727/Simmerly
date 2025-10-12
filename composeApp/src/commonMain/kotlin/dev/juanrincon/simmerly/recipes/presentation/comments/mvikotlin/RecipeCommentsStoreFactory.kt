package dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toCommentUi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class RecipeCommentsStoreFactory(
    private val recipeId: String,
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeCommentsStore =
        object : RecipeCommentsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeCommentsStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(recipeId),
            executorFactory = { ExecutorImpl(repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class LoadComments(val recipeId: String) : Action
    }

    private sealed interface Msg {
        data class CommentsLoaded(val comments: List<CommentUi>) : Msg
        data class CommentTextChange(val text: String) : Msg
    }

    private class BootstrapperImpl(private val recipeId: String) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadComments(recipeId))
        }
    }

    private class ExecutorImpl(private val repository: RecipeRepository) :
        CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.OnCommentTextChanged -> dispatch(Msg.CommentTextChange(intent.text))
                Intent.OnSendCommentClicked -> TODO()
            }
        }

        override fun executeAction(action: Action) {
            when (action) {
                is Action.LoadComments -> observeComments(action.recipeId)
            }
        }

        private fun observeComments(recipeId: String) {
            scope.launch {
                repository.comments(recipeId).distinctUntilChanged()
                    .map { comments -> comments.map { it.toCommentUi() } }
                    .collect { comments ->
                        dispatch(Msg.CommentsLoaded(comments))
                    }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.CommentsLoaded -> copy(comments = msg.comments)
                is Msg.CommentTextChange -> copy(commentText = msg.text)
            }
    }
}
